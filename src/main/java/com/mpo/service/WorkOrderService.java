package com.mpo.service;

import org.springframework.stereotype.Service;

import com.mpo.entity.MaterialSectionType;
import com.mpo.entity.WorkOrder;
import com.mpo.repository.WorkOrderRepository;

import lombok.RequiredArgsConstructor;
import java.util.List;
@Service
@RequiredArgsConstructor
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;

    public WorkOrder saveWorkOrder(WorkOrder workOrder) {

        Double prepLength = calculatePrepLength(workOrder.getPartLength(), workOrder.getTechnicalAllowance());
        Double density = workOrder.getMaterialType().getDensity();
        Double partMass = calculatePrepMass(workOrder.getMaterialSectionType(), workOrder.getPartLength(), density);
        Double blankMass = calculatePrepMass(workOrder.getMaterialSectionType(), prepLength, density);
        Double removedMass = massForRemoval(blankMass, partMass);

        workOrder.setPrepLength(prepLength);
        workOrder.setPartMass(partMass);
        workOrder.setBlankMass(blankMass);
        workOrder.setRemovedMass(removedMass);

        return workOrderRepository.save(workOrder);
    }

    public List<WorkOrder> getAll() {
        return workOrderRepository.findAll();
    }

    public WorkOrder getById(String id) {
        return workOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Radni nalog nije pronađen: " + id));
    }
}