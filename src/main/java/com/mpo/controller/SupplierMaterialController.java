package com.mpo.controller;

import com.mpo.entity.MaterialSectionType;
import com.mpo.entity.MaterialType;
import com.mpo.entity.SupplierMaterial;
import com.mpo.enums.SectionShape;
import com.mpo.service.MaterialSectionTypeService;
import com.mpo.service.MaterialTypeService;
import com.mpo.service.SupplierMaterialService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supplier-materials")
@RequiredArgsConstructor
public class SupplierMaterialController {

    private final SupplierMaterialService supplierMaterialService;
    private final MaterialTypeService materialTypeService;
    private final MaterialSectionTypeService materialSectionTypeService;

    @GetMapping
    public List<SupplierMaterial> getAll() {
        return supplierMaterialService.getAll();
    }

    @GetMapping("/{id}")
    public SupplierMaterial getById(@PathVariable Integer id) {
        return supplierMaterialService.getById(id);
    }

    @PostMapping
    public SupplierMaterial save(@RequestBody SupplierMaterial supplierMaterial) {
        return supplierMaterialService.saveSupplierMaterial(supplierMaterial);
    }

    @GetMapping("/optimal")
    public SupplierMaterial findOptimal(
            @RequestParam String materialTypeName,
            @RequestParam SectionShape materialSectionTypeName,
            @RequestParam Double weightPrice,
            @RequestParam Double weightDeliveryTime) {

        MaterialType materialType = materialTypeService.getByMaterialTypeName(materialTypeName);
        MaterialSectionType materialSectionType = materialSectionTypeService.getByTypeName(materialSectionTypeName);

        List<SupplierMaterial> offers = supplierMaterialService.findOffersForMaterial(materialType, materialSectionType);

        return supplierMaterialService.findOptimal(offers, weightPrice, weightDeliveryTime);
    }
}
