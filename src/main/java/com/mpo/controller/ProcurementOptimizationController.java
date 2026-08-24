package com.mpo.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mpo.dto.OptimizationResult;
import com.mpo.entity.WorkOrder;
import com.mpo.service.ProcurementOptimizationService;
import com.mpo.service.WorkOrderService;

@RestController
@RequestMapping("/api/procurement-optimization")
public class ProcurementOptimizationController {

    private final ProcurementOptimizationService procurementOptimizationService;
    private final WorkOrderService workOrderService;

    public ProcurementOptimizationController(ProcurementOptimizationService procurementOptimizationService,
                                              WorkOrderService workOrderService) {
        this.procurementOptimizationService = procurementOptimizationService;
        this.workOrderService = workOrderService;
    }

    @GetMapping("/optimize/{workOrderId}")
    public OptimizationResult optimizeProcurement(@PathVariable String workOrderId,
                                                   @RequestParam Double weightPrice,
                                                   @RequestParam Double weightDeliveryTime) {

        WorkOrder workOrder = workOrderService.getById(workOrderId);

        return procurementOptimizationService.optimizeProcurementForWorkOrder(workOrder, weightPrice, weightDeliveryTime);
    }
}
