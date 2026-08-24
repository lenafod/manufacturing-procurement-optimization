package com.mpo.service;

import org.springframework.stereotype.Service;

import com.mpo.entity.PurchaseRequest;
import com.mpo.enums.PurchaseRequestStatus;
import com.mpo.exception.InvalidRequestException;
import com.mpo.exception.ResourceNotFoundException;
import com.mpo.repository.PurchaseRequestRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class PurchaseRequestService {

    private final PurchaseRequestRepository purchaseRequestRepository;
    private final InventoryService inventoryService;

    public PurchaseRequestService(PurchaseRequestRepository purchaseRequestRepository, InventoryService inventoryService) {
        this.purchaseRequestRepository = purchaseRequestRepository;
        this.inventoryService = inventoryService;
    }

    public List<PurchaseRequest> getPurchaseRequestsByStatus(PurchaseRequestStatus status) {
        return purchaseRequestRepository.findByStatus(status);
    }

    public List<PurchaseRequest> getAllPurchaseRequests() {
        return purchaseRequestRepository.findAll();
    }

    public List<PurchaseRequest> getPurchaseRequestsByWorkOrderId(String workOrderId) {
        return purchaseRequestRepository.findByTechnicalSheet_WorkOrder_Id(workOrderId);
    }

    public List<PurchaseRequest> getOverduePurchaseRequests() {
        List<PurchaseRequestStatus> excludedStatuses = List.of(PurchaseRequestStatus.DELIVERED, PurchaseRequestStatus.CANCELED);
        return purchaseRequestRepository.findByStatusNotInAndExpectedDeliveryDateBefore(excludedStatuses, LocalDate.now());
    }

    public PurchaseRequest getPurchaseRequestById(Long id) {
        return purchaseRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("purchase request with id " + id + " not found"));
    }

    public PurchaseRequest save(PurchaseRequest purchaseRequest) {
        return purchaseRequestRepository.save(purchaseRequest);
    }

    // sprecava duplirane zahteve - ako vec postoji nezavrsen zahtev za ovu poziciju, ne pravi se novi
    public boolean hasActiveRequestForTechnicalSheet(String technicalSheetId) {
        List<PurchaseRequestStatus> excludedStatuses = List.of(PurchaseRequestStatus.DELIVERED, PurchaseRequestStatus.CANCELED);
        return purchaseRequestRepository.existsByTechnicalSheet_IdAndStatusNotIn(technicalSheetId, excludedStatuses);
    }

    public PurchaseRequest updateStatus(Long purchaseRequestId, PurchaseRequestStatus newStatus) {
        PurchaseRequest purchaseRequest = purchaseRequestRepository.findById(purchaseRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("purchase request with id " + purchaseRequestId + " not found"));
        validateStatusChange(purchaseRequest, newStatus);
        purchaseRequest.setStatus(newStatus);

        if(newStatus == PurchaseRequestStatus.DELIVERED) {
            purchaseRequest.setActualDeliveryDate(LocalDate.now());
            inventoryService.increaseQuantity(
                    purchaseRequest.getTechnicalSheet().getMaterialType(),
                    purchaseRequest.getTechnicalSheet().getMaterialSectionType(),
                    purchaseRequest.getRequiredQuantity()
            );
        }

        return purchaseRequestRepository.save(purchaseRequest);
    }

    private void validateStatusChange(PurchaseRequest purchaseRequest, PurchaseRequestStatus newStatus) {
        PurchaseRequestStatus currentStatus = purchaseRequest.getStatus();

        if (currentStatus == PurchaseRequestStatus.CANCELED) {
            if (newStatus != PurchaseRequestStatus.CANCELED) {
                throw new InvalidRequestException("Cannot change status of a canceled purchase request");
            }
            return;
        }

        if (newStatus == PurchaseRequestStatus.CANCELED) {
            return;
        }

        List<PurchaseRequestStatus> linearFlow = List.of(
                PurchaseRequestStatus.CREATED,
                PurchaseRequestStatus.SENT,
                PurchaseRequestStatus.IN_DELIVERY,
                PurchaseRequestStatus.DELIVERED
        );

        if (linearFlow.indexOf(newStatus) < linearFlow.indexOf(currentStatus)) {
            throw new InvalidRequestException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }
    }
}
