package com.mpo.controller;

import com.mpo.entity.Inventory;
import com.mpo.entity.MaterialSectionType;
import com.mpo.entity.MaterialType;
import com.mpo.service.InventoryService;
import com.mpo.service.MaterialSectionTypeService;
import com.mpo.service.MaterialTypeService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final MaterialTypeService materialTypeService;
    private final MaterialSectionTypeService materialSectionTypeService;

    @GetMapping
    public List<Inventory> getAll() {
        return inventoryService.getAll();
    }

    @GetMapping("/{id}")
    public Inventory getById(@PathVariable Long id) {
        return inventoryService.getById(id);
    }  

    @PostMapping
    public Inventory save(@RequestBody Inventory inventory) {
        return inventoryService.save(inventory);
    }

    @GetMapping("/check")
    public boolean checkInventory(
            @RequestParam String materialTypeName,
            @RequestParam Integer materialSectionTypeId,
            @RequestParam Double requiredQuantity) {

        MaterialType materialType = materialTypeService.getByMaterialTypeName(materialTypeName);
        MaterialSectionType materialSectionType = materialSectionTypeService.getById(materialSectionTypeId);

        return inventoryService.checkInventory(materialType, materialSectionType, requiredQuantity);
    }
}