package com.mpo.service;

import org.springframework.stereotype.Service;

import com.mpo.entity.MaterialSectionType;
import com.mpo.entity.WorkOrder;
import com.mpo.entity.TechnicalSheet;
import com.mpo.repository.WorkOrderRepository;
import com.mpo.repository.TechnicalSheetRepository;

import lombok.RequiredArgsConstructor;
import java.util.List;
@Service
@RequiredArgsConstructor
public class TechnicalSheetService {

    private final TechnicalSheetRepository technicalSheetRepository;

    public TechnicalSheet saveTechnicalSheet(TechnicalSheet technicalSheet) {
    
        Double prepLength = calculatePrepLength(technicalSheet.getPartLength(), technicalSheet.getTechnicalAllowance());
        Double density = technicalSheet.getMaterialType().getDensity();
        Double partMass = calculatePrepMass(technicalSheet.getMaterialSectionType(), technicalSheet.getPartLength(), density);
        Double blankMass = calculatePrepMass(technicalSheet.getMaterialSectionType(), prepLength, density);
        Double removedMass = massForRemoval(blankMass, partMass);

        technicalSheet.setPrepLength(prepLength);
        technicalSheet.setPartMass(partMass);
        technicalSheet.setBlankMass(blankMass);
        technicalSheet.setRemovedMass(removedMass);

        return technicalSheetRepository.save(technicalSheet);
    }

    public List<TechnicalSheet> getAll() {
        return technicalSheetRepository.findAll();
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