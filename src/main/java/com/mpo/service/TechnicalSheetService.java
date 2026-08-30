package com.mpo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mpo.entity.MaterialSectionType;
import com.mpo.entity.TechnicalSheet;
import com.mpo.exception.InvalidRequestException;
import com.mpo.exception.ResourceNotFoundException;
import com.mpo.repository.TechnicalSheetRepository;

import static com.mpo.enums.SectionShape.*;

import lombok.RequiredArgsConstructor;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.data.domain.Sort;
@Service
@RequiredArgsConstructor
public class TechnicalSheetService {

    private final TechnicalSheetRepository technicalSheetRepository;
    private final MaterialTypeService materialTypeService;
    private final MaterialSectionTypeService materialSectionTypeService;

    @Value("${procurement.uploads.drawings-dir}")
    private String drawingsDir;

    public List<TechnicalSheet> getTechnicalSheetsBySheetId(String sheetId, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by("sheetVersion").ascending() : Sort.by("sheetVersion").descending();

        return technicalSheetRepository.findAll((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("id"), sheetId), sort);
    }

    public TechnicalSheet getTechnicalSheetByIdAndVersion(String id, String version) {
        return technicalSheetRepository.findAll((root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("id"), id),
                        criteriaBuilder.equal(root.get("sheetVersion"), version)
                )).stream().findFirst().orElseThrow(() -> new ResourceNotFoundException("Technical sheet not found"));
    }

    public TechnicalSheet saveTechnicalSheet(TechnicalSheet technicalSheet) {

        // klijent salje samo {id: X} reference za materialType/materialSectionType - moraju se
        // ucitati puni entiteti pre proracuna, jer stub objekat ima sva ostala polja null
        MaterialSectionType materialSectionType = materialSectionTypeService.getById(technicalSheet.getMaterialSectionType().getId());
        technicalSheet.setMaterialSectionType(materialSectionType);
        technicalSheet.setMaterialType(materialTypeService.getById(technicalSheet.getMaterialType().getId()));

        Double prepLength = calculatePrepLength(technicalSheet.getPartLength(), technicalSheet.getTechnicalAllowance());
        Double density = technicalSheet.getMaterialType().getDensity();
        Double partMass = calculatePrepMass(materialSectionType, technicalSheet.getPartLength(), density);
        Double blankMass = calculatePrepMass(materialSectionType, prepLength, density);
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

    public TechnicalSheet getById(String id) {
        return technicalSheetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("technical sheet with id " + id + " not found"));
    }

    // crtez se cuva na disku kao {technicalSheetId}.{ekstenzija} - jedan fajl po poziciji (novi
    // upload prepisuje stari). baza pamti samo originalni naziv, sluzi za Content-Disposition
    // pri preuzimanju i da front zna da li je crtez uopste otpremljen (drawingFileName != null)
    public TechnicalSheet uploadDrawing(String id, MultipartFile file) {
        TechnicalSheet technicalSheet = getById(id);

        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("uploaded file must not be empty");
        }

        String extension = extractExtension(file.getOriginalFilename());

        try {
            Path dir = Path.of(drawingsDir);
            Files.createDirectories(dir);
            Path target = dir.resolve(id + extension);
            file.transferTo(target);
        } catch (IOException e) {
            throw new UncheckedIOException("failed to store drawing for technical sheet " + id, e);
        }

        technicalSheet.setDrawingFileName(file.getOriginalFilename());
        return technicalSheetRepository.save(technicalSheet);
    }

    public Resource getDrawingResource(String id) {
        TechnicalSheet technicalSheet = getById(id);

        if (technicalSheet.getDrawingFileName() == null) {
            throw new ResourceNotFoundException("technical sheet with id " + id + " has no uploaded drawing");
        }

        String extension = extractExtension(technicalSheet.getDrawingFileName());
        Path target = Path.of(drawingsDir).resolve(id + extension);
        return new FileSystemResource(target);
    }

    // technicalSheet.id je vec ljudski citljiv (sheetId + "-" + verzija) i koristi se direktno kao
    // naziv fajla na disku - uzimamo samo ekstenziju iz ORIGINALNOG imena, ne ceo naziv, da upload
    // ne moze da izadje van drawingsDir (path traversal) preko zlonamerno sastavljenog imena fajla
    private String extractExtension(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0) {
            return "";
        }
        return originalFilename.substring(dotIndex).replaceAll("[^a-zA-Z0-9.]", "");
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
            case ROUND -> (Math.PI / 4) * sectionType.getDim1() * sectionType.getDim1() * partLength;
            case CUBE -> sectionType.getDim1() * sectionType.getDim1() * partLength;
            case RECTANGULAR -> sectionType.getDim1() * sectionType.getDim2() * partLength;
            case HEXAGONAL -> 0.866 * sectionType.getDim1() * sectionType.getDim1() * partLength;
            case PIPE -> (Math.PI / 4)
                    * (sectionType.getDim1() * sectionType.getDim1() - sectionType.getDim2() * sectionType.getDim2())
                    * partLength;
        };
    }
}