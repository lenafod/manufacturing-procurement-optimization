package com.mpo.service;

import org.springframework.stereotype.Service;
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

    public List<PurchaseRequest> optimizeProcurementForWorkOrder(WorkOrder workOrder, Double weightPrice, Double weightDeliveryTime) {
        List<PurchaseRequest> purchaseRequests = new ArrayList<>();

        for (TechnicalSheet technicalSheet : workOrder.getTechnicalSheets()) {
            PurchaseRequest purchaseRequest = optimizeProcurement(technicalSheet, weightPrice, weightDeliveryTime);
            if (purchaseRequest != null) {
                purchaseRequests.add(purchaseRequestService.save(purchaseRequest));
            }
        }

        return purchaseRequests;
    }

    //ova metoda za pojedinacno
    public PurchaseRequest optimizeProcurement(TechnicalSheet technicalSheet, Double weightPrice, Double weightDeliveryTime) {

        if (weightPrice == null || weightDeliveryTime == null || weightPrice < 0 || weightDeliveryTime < 0) {
            throw new InvalidRequestException("weightPrice i weightDeliveryTime must be non-negative numbers");
        }

        if (Math.abs(weightPrice + weightDeliveryTime - 1.0) > 0.001) {
            throw new InvalidRequestException("weightPrice i weightDeliveryTime must sum to 1");
        }

        double neededLength = technicalSheet.getPrepLength() * technicalSheet.getQuantity();

        boolean isAvailable = inventoryService.checkInventory(technicalSheet.getMaterialType(), technicalSheet.getMaterialSectionType(), neededLength);

        if (isAvailable) {
            return null;
        }

        SupplierMaterial optimalOffer = supplierMaterialService.findOptimal(
                supplierMaterialService.findOffersForMaterial(technicalSheet.getMaterialType(), technicalSheet.getMaterialSectionType()),
                weightPrice, weightDeliveryTime
        );

        LocalDate createdAt = LocalDate.now();

        PurchaseRequest purchaseRequest = new PurchaseRequest();
        purchaseRequest.setTechnicalSheet(technicalSheet);
        purchaseRequest.setSupplierMaterial(optimalOffer);
        purchaseRequest.setRequiredQuantity(neededLength);
        purchaseRequest.setTotalPrice(neededLength * optimalOffer.getPricePerUnit());
        purchaseRequest.setStatus(PurchaseRequestStatus.CREATED);
        purchaseRequest.setCreatedAt(createdAt);
        purchaseRequest.setExpectedDeliveryDate(createdAt.plusDays(optimalOffer.getDeliveryTime()));

        return purchaseRequest;
    }
}
