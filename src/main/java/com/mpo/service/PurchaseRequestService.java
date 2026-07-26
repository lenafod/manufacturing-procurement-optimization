package com.mpo.service;

import org.springframework.stereotype.Service;

import com.mpo.entity.PurchaseRequest;
import com.mpo.enums.PurchaseRequestStatus;
import com.mpo.repository.PurchaseRequestRepository;

import java.util.List;

@Service
public class PurchaseRequestService {

    private final PurchaseRequestRepository purchaseRequestRepository;

    public PurchaseRequestService(PurchaseRequestRepository purchaseRequestRepository) {
        this.purchaseRequestRepository = purchaseRequestRepository;
    }

    public List<PurchaseRequest> getPurchaseRequestsByStatus(PurchaseRequestStatus status) {
        return purchaseRequestRepository.findByStatus(status);
    }

    public PurchaseRequest save(PurchaseRequest purchaseRequest) {
        return purchaseRequestRepository.save(purchaseRequest);
    }
}
