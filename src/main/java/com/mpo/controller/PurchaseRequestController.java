package com.mpo.controller;

import org.springframework.web.bind.annotation.RestController;
import com.mpo.service.PurchaseRequestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mpo.entity.PurchaseRequest;
import com.mpo.enums.PurchaseRequestStatus;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-requests")
public class PurchaseRequestController {

    private final PurchaseRequestService purchaseRequestService;

    public PurchaseRequestController(PurchaseRequestService purchaseRequestService) {
        this.purchaseRequestService = purchaseRequestService;
    }

    @GetMapping("/by-status")
    public List<PurchaseRequest> getPurchaseRequestsByStatus(@RequestParam("status") PurchaseRequestStatus status) {
        return purchaseRequestService.getPurchaseRequestsByStatus(status);
    }

    @GetMapping
    public List<PurchaseRequest> getAllPurchaseRequests() {
        return purchaseRequestService.getAllPurchaseRequests();
    }

    @GetMapping("/by-work-order")
    public List<PurchaseRequest> getPurchaseRequestsByWorkOrder(@RequestParam String workOrderId) {
        return purchaseRequestService.getPurchaseRequestsByWorkOrderId(workOrderId);
    }

    @GetMapping("/overdue")
    public List<PurchaseRequest> getOverduePurchaseRequests() {
        return purchaseRequestService.getOverduePurchaseRequests();
    }

    @GetMapping("/{id}")
    public PurchaseRequest getPurchaseRequestById(@PathVariable Long id) {
        return purchaseRequestService.getPurchaseRequestById(id);
    }

    @PatchMapping("/{id}/status")
    public PurchaseRequest updateStatus(@PathVariable Long id, @RequestParam PurchaseRequestStatus status) {
        return purchaseRequestService.updateStatus(id, status);
    }

}
