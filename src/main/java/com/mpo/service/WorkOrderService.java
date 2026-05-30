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

    private Double calculatePrepLength(Double partLength, Double technicalAllowance) {
        return partLength + technicalAllowance;
    }

    private Double calculatePrepMass(MaterialSectionType sectionType, Double prepLength, Double density) {
        Double volume = calculateVolume(sectionType, prepLength);
        return (volume / 1000) * density; // density nadam se imam u material type
    }

    private Double massForRemoval(Double prepMass, Double partMass) {
        return prepMass - partMass;
    }

    private Double calculateVolume(MaterialSectionType sectionType, Double partLength) {

        return switch (sectionType.getTypeName()) {
            case "ROUND" -> (Math.PI / 4) * sectionType.getDim1() * sectionType.getDim1() * partLength;
            case "SQUARE" -> sectionType.getDim1() * sectionType.getDim1() * partLength;
            case "RECTANGLE" -> sectionType.getDim1() * sectionType.getDim2() * partLength;
            case "HEX" -> 0.866 * sectionType.getDim1() * sectionType.getDim1() * partLength;
            case "TUBE" -> (Math.PI / 4)
                    * (sectionType.getDim1() * sectionType.getDim1() - sectionType.getDim2() * sectionType.getDim2())
                    * partLength;

            default -> throw new RuntimeException("Nepoznat oblik preseka: " + sectionType.getTypeName());
        };
    }

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