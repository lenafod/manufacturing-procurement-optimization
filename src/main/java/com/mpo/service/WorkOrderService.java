package com.mpo.service;

import org.springframework.stereotype.Service;

import com.mpo.entity.MaterialSectionType;
import com.mpo.entity.WorkOrder;
import com.mpo.exception.DuplicateResourceException;
import com.mpo.exception.ResourceNotFoundException;
import com.mpo.repository.WorkOrderRepository;

import lombok.RequiredArgsConstructor;
import java.util.List;
@Service
@RequiredArgsConstructor
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;

    public WorkOrder saveWorkOrder(WorkOrder workOrder) {
        if (workOrderRepository.existsById(workOrder.getId())) {
            throw new DuplicateResourceException("work order with id " + workOrder.getId() + " already exists");
        }
        return workOrderRepository.save(workOrder);
    }

    public List<WorkOrder> getAll() {
        return workOrderRepository.findAll();
    }

    public WorkOrder getById(String id) {
        return workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("work order not found: " + id));
    }
}