package com.mpo.controller;

import com.mpo.entity.TechnicalSheet;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.mpo.service.TechnicalSheetService;
import com.mpo.service.PdfService;

@RestController
@RequestMapping("/api/technical-sheets")
@RequiredArgsConstructor
public class TechnicalSheetController {

    private final TechnicalSheetService technicalSheetService;
    private final PdfService pdfService;

    @GetMapping("/by-sheet-id")
    public List<TechnicalSheet> getTechnicalSheetsBySheetId(@RequestParam String sheetId, @RequestParam String sortDirection) {
        return technicalSheetService.getTechnicalSheetsBySheetId(sheetId, sortDirection);
    }

    @GetMapping("/by-id-and-version")
    public TechnicalSheet getTechnicalSheetByIdAndVersion(@RequestParam String id, @RequestParam String version) {
         return technicalSheetService.getTechnicalSheetByIdAndVersion(id, version);
    }

    @PostMapping
    public TechnicalSheet save(@RequestBody TechnicalSheet technicalSheet) {
        return technicalSheetService.saveTechnicalSheet(technicalSheet);
    }



//front ce da poziva ovu metodu tako da se preview-uje crtez, a ne downloaduje pdf
    @GetMapping("/by-id-and-version/pdf")
    public ResponseEntity<byte[]> getTechnicalSheetPdfByIdAndVersion(@RequestParam String id, @RequestParam String version) throws Exception {
        TechnicalSheet technicalSheet = technicalSheetService.getTechnicalSheetByIdAndVersion(id, version);
        byte[] pdfBytes = pdfService.generateTechnicalSheetPdf(technicalSheet);

        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=technical_sheet_" + id + "_v" + version + ".pdf")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping("/{id}/drawing")
    public TechnicalSheet uploadDrawing(@PathVariable String id, @RequestParam("file") MultipartFile file) {
        return technicalSheetService.uploadDrawing(id, file);
    }

    @GetMapping("/{id}/drawing")
    public ResponseEntity<Resource> getDrawing(@PathVariable String id) throws IOException {
        TechnicalSheet technicalSheet = technicalSheetService.getById(id);
        Resource resource = technicalSheetService.getDrawingResource(id);
        String contentType = Files.probeContentType(resource.getFile().toPath());

        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=" + technicalSheet.getDrawingFileName())
                .contentType(contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

}
