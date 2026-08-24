package com.mpo.service;

import org.springframework.stereotype.Service;
import com.mpo.dto.OptimizationResult;
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
import java.util.List;

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
        validateWeights(weightPrice, weightDeliveryTime);

        List<PurchaseRequest> created = new ArrayList<>();
        List<SkippedPosition> skipped = new ArrayList<>();

        for (TechnicalSheet technicalSheet : workOrder.getTechnicalSheets()) {
            PositionOutcome outcome = evaluatePosition(technicalSheet, weightPrice, weightDeliveryTime);

            if (outcome instanceof PositionOutcome.Created createdOutcome) {
                created.add(purchaseRequestService.save(createdOutcome.purchaseRequest()));
            } else if (outcome instanceof PositionOutcome.Skipped skippedOutcome) {
                skipped.add(new SkippedPosition(technicalSheet.getPositionName(), skippedOutcome.reason()));
            }
        }

        return new OptimizationResult(created, skipped);
    }

    private void validateWeights(Double weightPrice, Double weightDeliveryTime) {
        if (weightPrice == null || weightDeliveryTime == null || weightPrice < 0 || weightDeliveryTime < 0) {
            throw new InvalidRequestException("weightPrice i weightDeliveryTime must be non-negative numbers");
        }

        if (Math.abs(weightPrice + weightDeliveryTime - 1.0) > 0.001) {
            throw new InvalidRequestException("weightPrice i weightDeliveryTime must sum to 1");
        }
    }

    private PositionOutcome evaluatePosition(TechnicalSheet technicalSheet, Double weightPrice, Double weightDeliveryTime) {
        if (purchaseRequestService.hasActiveRequestForTechnicalSheet(technicalSheet.getId())) {
            return new PositionOutcome.Skipped("Već postoji aktivan zahtev za nabavku ove pozicije");
        }

        double neededLength = technicalSheet.getPrepLength() * technicalSheet.getQuantity();

        boolean isAvailable = inventoryService.checkInventory(technicalSheet.getMaterialType(), technicalSheet.getMaterialSectionType(), neededLength);

        if (isAvailable) {
            return new PositionOutcome.Skipped("Ima dovoljno materijala na lageru");
        }

        List<SupplierMaterial> offers = supplierMaterialService.findOffersForMaterial(technicalSheet.getMaterialType(), technicalSheet.getMaterialSectionType());

        if (offers.isEmpty()) {
            return new PositionOutcome.Skipped("Nema ponuda dobavljača za ovaj materijal i presek");
        }

        SupplierMaterial optimalOffer = supplierMaterialService.findOptimal(offers, weightPrice, weightDeliveryTime);

        LocalDate createdAt = LocalDate.now();

        PurchaseRequest purchaseRequest = new PurchaseRequest();
        purchaseRequest.setTechnicalSheet(technicalSheet);
        purchaseRequest.setSupplierMaterial(optimalOffer);
        purchaseRequest.setRequiredQuantity(neededLength);
        purchaseRequest.setTotalPrice(neededLength * optimalOffer.getPricePerUnit());
        purchaseRequest.setStatus(PurchaseRequestStatus.CREATED);
        purchaseRequest.setCreatedAt(createdAt);
        purchaseRequest.setExpectedDeliveryDate(createdAt.plusDays(optimalOffer.getDeliveryTime()));

        return new PositionOutcome.Created(purchaseRequest);
    }

    private sealed interface PositionOutcome {
        record Created(PurchaseRequest purchaseRequest) implements PositionOutcome {
        }

        record Skipped(String reason) implements PositionOutcome {
        }
    }
}
