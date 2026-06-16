package com.mpo.controller;

import com.mpo.entity.TechnicalSheet;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

import com.mpo.service.TechnicalSheetService;

@RestController
@RequestMapping("/api/technical-sheets")
@RequiredArgsConstructor
public class TechnicalSheetController {

    private final TechnicalSheetService technicalSheetService;

    @GetMapping("/by-sheet-id")
    public List<TechnicalSheet> getTechnicalSheetsBySheetId(@RequestParam String sheetId, @RequestParam String sortDirection) {
        return technicalSheetService.getTechnicalSheetsBySheetId(sheetId, sortDirection);
    }

    @GetMapping("/by-id-and-version")
    public TechnicalSheet getTechnicalSheetByIdAndVersion(@RequestParam String id, @RequestParam String version) {
         return technicalSheetService.getTechnicalSheetByIdAndVersion(id, version);
    }

}
