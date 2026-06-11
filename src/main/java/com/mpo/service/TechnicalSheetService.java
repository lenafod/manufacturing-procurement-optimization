package com.mpo.service;

import org.springframework.stereotype.Service;

import com.mpo.entity.MaterialSectionType;
import com.mpo.entity.WorkOrder;
import com.mpo.repository.WorkOrderRepository;

import lombok.RequiredArgsConstructor;
import java.util.List;
@Service
@RequiredArgsConstructor
public class TechnicalSheetService {

    public TechnicalSheet saveTechnicalSheet(TechnicalSheet technicalSheet) {
        Double prepLength = calculatePrepLength(technicalSheet.getPartLength(), technicalSheet.getTechnicalAllowance());
        Double density = technicalSheet.getMaterialType().getDensity();
        Double partMass = calculate
    }

    public List<TechnicalSheet> getAll() {
        return technicalSheetRepository.findAll();
    }

    public TechnicalSheet getById(Long id) {
        return technicalSheetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tehnički list nije pronađen: " + id));
    }

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
}