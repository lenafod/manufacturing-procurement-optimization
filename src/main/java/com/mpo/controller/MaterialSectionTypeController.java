package com.mpo.controller;

import com.mpo.entity.MaterialSectionType;
import com.mpo.service.MaterialSectionTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/material-section-types")
@RequiredArgsConstructor
public class MaterialSectionTypeController {

    private final MaterialSectionTypeService materialSectionTypeService;

    @GetMapping
    public List<MaterialSectionType> getAll() {
        return materialSectionTypeService.getAll();
    }

    @GetMapping("/{id}")
    public MaterialSectionType getById(@PathVariable Integer id) {
        return materialSectionTypeService.getById(id);
    }
}