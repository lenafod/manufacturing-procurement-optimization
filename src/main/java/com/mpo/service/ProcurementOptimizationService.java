package com.mpo.service;

import org.springframework.stereotype.Service;
import com.mpo.dto.OptimizationResult;
import com.mpo.dto.OptimizationResult.PartialFulfillment;
import com.mpo.dto.OptimizationResult.SkippedPosition;
import com.mpo.entity.PurchaseRequest;
import com.mpo.enums.PurchaseRequestStatus;
import com.mpo.exception.InvalidRequestException;
import com.mpo.service.PurchaseRequestService;
import com.mpo.service.InventoryService;
import com.mpo.entity.TechnicalSheet;
import com.mpo.entity.WorkOrder;
import com.mpo.service.SupplierMaterialService;
import com.mpo.entity.SupplierMaterial;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProcurementOptimizationService {

    private final InventoryService inventoryService;
    private final SupplierMaterialService supplierMaterialService;
    private final PurchaseRequestService purchaseRequestService;

    public ProcurementOptimizationService(InventoryService inventoryService, SupplierMaterialService supplierMaterialService, PurchaseRequestService purchaseRequestService) {
        this.inventoryService = inventoryService;
        this.supplierMaterialService = supplierMaterialService;
        this.purchaseRequestService = purchaseRequestService;
    }

    public OptimizationResult optimizeProcurementForWorkOrder(WorkOrder workOrder, Double weightPrice, Double weightDeliveryTime) {
        return runOptimization(workOrder, weightPrice, weightDeliveryTime, true);
    }

    // "suvo" izvrsavanje - racuna istu alokaciju kao optimizeProcurementForWorkOrder (ista pravila,
    // ista rangiranja), ali ne upisuje nista u bazu: ne pravi PurchaseRequest redove i ne umanjuje
    // availableQuantity kod dobavljaca. Sluzi da korisnik vidi predlog pre nego sto ga potvrdi.
    public OptimizationResult previewProcurementForWorkOrder(WorkOrder workOrder, Double weightPrice, Double weightDeliveryTime) {
        return runOptimization(workOrder, weightPrice, weightDeliveryTime, false);
    }

    private OptimizationResult runOptimization(WorkOrder workOrder, Double weightPrice, Double weightDeliveryTime, boolean commit) {
        validateWeights(weightPrice, weightDeliveryTime);

        List<PurchaseRequest> created = new ArrayList<>();
        List<PartialFulfillment> partial = new ArrayList<>();
        List<SkippedPosition> skipped = new ArrayList<>();

        // prati koliko je vec "rezervisano" po ponudi/lageru u okviru ovog poziva - samo za preview
        // (commit=true odmah upisuje umanjenje u bazu pa svaka pozicija cita svezo stanje)
        Map<Integer, Double> previewSupplierLedger = new HashMap<>();
        Map<String, Double> previewInventoryLedger = new HashMap<>();

        for (TechnicalSheet technicalSheet : workOrder.getTechnicalSheets()) {
            PositionOutcome outcome = evaluatePosition(technicalSheet, weightPrice, weightDeliveryTime, commit,
                    previewSupplierLedger, previewInventoryLedger);

            if (outcome instanceof PositionOutcome.Created createdOutcome) {
                if (commit) {
                    createdOutcome.purchaseRequests().forEach(pr -> created.add(purchaseRequestService.save(pr)));
                } else {
                    created.addAll(createdOutcome.purchaseRequests());
                }
            } else if (outcome instanceof PositionOutcome.PartiallyCreated partialOutcome) {
                if (commit) {
                    partialOutcome.purchaseRequests().forEach(pr -> created.add(purchaseRequestService.save(pr)));
                } else {
                    created.addAll(partialOutcome.purchaseRequests());
                }
                partial.add(new PartialFulfillment(technicalSheet.getPositionName(), partialOutcome.missingQuantity()));
            } else if (outcome instanceof PositionOutcome.Skipped skippedOutcome) {
                skipped.add(new SkippedPosition(technicalSheet.getPositionName(), skippedOutcome.reason()));
            }
        }

        return new OptimizationResult(created, partial, skipped);
    }

    private void validateWeights(Double weightPrice, Double weightDeliveryTime) {
        if (weightPrice == null || weightDeliveryTime == null || weightPrice < 0 || weightDeliveryTime < 0) {
            throw new InvalidRequestException("weightPrice i weightDeliveryTime must be non-negative numbers");
        }

        if (Math.abs(weightPrice + weightDeliveryTime - 1.0) > 0.001) {
            throw new InvalidRequestException("weightPrice i weightDeliveryTime must sum to 1");
        }
    }

    private PositionOutcome evaluatePosition(TechnicalSheet technicalSheet, Double weightPrice, Double weightDeliveryTime,
                                              boolean commit, Map<Integer, Double> previewSupplierLedger,
                                              Map<String, Double> previewInventoryLedger) {
        if (purchaseRequestService.hasActiveRequestForTechnicalSheet(technicalSheet.getId())) {
            return new PositionOutcome.Skipped("Već postoji aktivan zahtev za nabavku ove pozicije");
        }

        double neededLength = technicalSheet.getPrepLength() * technicalSheet.getQuantity();

        // prvo se koristi ono sto vec postoji na lageru, pa se dobavljacima trazi samo razlika -
        // umesto da se (kao ranije) od dobavljaca odmah trazi cela potrebna kolicina cim lager
        // nije DOVOLJAN, cak i ako delimicno pokriva potrebu
        String inventoryKey = technicalSheet.getMaterialType().getId() + ":" + technicalSheet.getMaterialSectionType().getId();
        Map<String, Double> inventoryLedger = commit ? new HashMap<>() : previewInventoryLedger;
        double reservedFromInventory = inventoryLedger.getOrDefault(inventoryKey, 0.0);
        double availableInInventory = Math.max(0,
                inventoryService.getAvailableQuantity(technicalSheet.getMaterialType(), technicalSheet.getMaterialSectionType()) - reservedFromInventory);
        double usedFromInventory = Math.min(availableInInventory, neededLength);
        double remainingNeeded = neededLength - usedFromInventory;

        if (remainingNeeded <= 0.0001) {
            applyInventoryUsage(technicalSheet, usedFromInventory, commit, inventoryLedger, inventoryKey);
            return new PositionOutcome.Skipped("Ima dovoljno materijala na lageru");
        }

        List<SupplierMaterial> offers = supplierMaterialService.findOffersForMaterial(technicalSheet.getMaterialType(), technicalSheet.getMaterialSectionType());

        if (offers.isEmpty()) {
            applyInventoryUsage(technicalSheet, usedFromInventory, commit, inventoryLedger, inventoryKey);
            return new PositionOutcome.Skipped(usedFromInventory > 0
                    ? "Iskorišćeno " + usedFromInventory + " sa lagera, ali nema ponuda dobavljača za preostalih " + remainingNeeded
                    : "Nema ponuda dobavljača za ovaj materijal i presek");
        }

        // commit=true: DB je odmah azurirana posle svake pozicije, pa svaka pozicija racuna sa
        // svezim stanjem - prazna, jednokratna mapa je dovoljna. commit=false (preview): deljena
        // previewSupplierLedger mapa prati rezervacije kroz sve pozicije istog poziva, bez upisa u bazu.
        Map<Integer, Double> reservedQuantities = commit ? new HashMap<>() : previewSupplierLedger;

        List<SupplierMaterialService.Allocation> allocations = supplierMaterialService.allocate(offers, remainingNeeded, weightPrice, weightDeliveryTime, reservedQuantities);

        if (allocations.isEmpty()) {
            applyInventoryUsage(technicalSheet, usedFromInventory, commit, inventoryLedger, inventoryKey);
            return new PositionOutcome.Skipped(usedFromInventory > 0
                    ? "Iskorišćeno " + usedFromInventory + " sa lagera, ali nijedan dobavljač trenutno nema raspoloživu količinu za preostalih " + remainingNeeded
                    : "Nijedan dobavljač trenutno nema raspoloživu količinu ovog materijala");
        }

        LocalDate createdAt = LocalDate.now();
        List<PurchaseRequest> purchaseRequests = new ArrayList<>();
        double allocatedTotal = 0;

        for (SupplierMaterialService.Allocation allocation : allocations) {
            SupplierMaterial offer = allocation.offer();
            double quantity = allocation.quantity();

            PurchaseRequest purchaseRequest = new PurchaseRequest();
            purchaseRequest.setTechnicalSheet(technicalSheet);
            purchaseRequest.setSupplierMaterial(offer);
            purchaseRequest.setRequiredQuantity(quantity);
            purchaseRequest.setTotalPrice(quantity * offer.getPricePerUnit());
            purchaseRequest.setStatus(PurchaseRequestStatus.CREATED);
            purchaseRequest.setCreatedAt(createdAt);
            purchaseRequest.setExpectedDeliveryDate(createdAt.plusDays(offer.getDeliveryTime()));

            purchaseRequests.add(purchaseRequest);
            if (commit) {
                supplierMaterialService.decreaseAvailableQuantity(offer, quantity);
            }
            allocatedTotal += quantity;
        }

        applyInventoryUsage(technicalSheet, usedFromInventory, commit, inventoryLedger, inventoryKey);

        double missingQuantity = remainingNeeded - allocatedTotal;

        if (missingQuantity > 0.0001) {
            return new PositionOutcome.PartiallyCreated(purchaseRequests, missingQuantity);
        }

        return new PositionOutcome.Created(purchaseRequests);
    }

    private void applyInventoryUsage(TechnicalSheet technicalSheet, double usedFromInventory, boolean commit,
                                      Map<String, Double> inventoryLedger, String inventoryKey) {
        if (usedFromInventory <= 0) {
            return;
        }
        if (commit) {
            inventoryService.decreaseQuantity(technicalSheet.getMaterialType(), technicalSheet.getMaterialSectionType(), usedFromInventory);
        } else {
            inventoryLedger.merge(inventoryKey, usedFromInventory, Double::sum);
        }
    }

    private sealed interface PositionOutcome {
        record Created(List<PurchaseRequest> purchaseRequests) implements PositionOutcome {
        }

        record PartiallyCreated(List<PurchaseRequest> purchaseRequests, double missingQuantity) implements PositionOutcome {
        }

        record Skipped(String reason) implements PositionOutcome {
        }
    }
}
