package com.mpo.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mpo.entity.Inventory;
import com.mpo.entity.MaterialSectionType;
import com.mpo.entity.MaterialType;
import com.mpo.repository.InventoryRepository;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private TechnicalSheetService technicalSheetService;

    @InjectMocks
    private InventoryService inventoryService;

    private MaterialType materialType;
    private MaterialSectionType materialSectionType;

    @BeforeEach
    void setUp() {
        materialType = new MaterialType();
        materialType.setId(1);
        materialType.setMaterialName("Steel");
        materialType.setDensity(7850.0);

        materialSectionType = new MaterialSectionType();
        materialSectionType.setId(1);
        materialSectionType.setTypeName("RECTANGLE");
        materialSectionType.setDim1(20.0);
        materialSectionType.setDim2(10.0);
        materialSectionType.setUsesDim2(true);
    }

    @Test
    void checkQuantity_returnsTrueWhenAvailableQuantityIsGreaterThanOrEqualPrepLength() {
        assertTrue(inventoryService.checkQuantity(1000.0, 1000.0));
        assertTrue(inventoryService.checkQuantity(1000.0, 1500.0));
    }

    @Test
    void checkQuantity_returnsFalseWhenAvailableQuantityIsLessThanPrepLength() {
        assertFalse(inventoryService.checkQuantity(1500.0, 1000.0));
    }

    @Test
    void checkInventory_returnsFalseWhenNoMatchingInventoryIsFound() {
        doNothing().when(technicalSheetService).validateDimensions(materialSectionType, 20.0, 10.0);
        when(inventoryRepository.findByMaterialTypeAndMaterialSectionTypeAndDim1AndDim2(
                eq(materialType), eq(materialSectionType), eq(20.0), eq(10.0)))
                .thenReturn(Optional.empty());

        boolean result = inventoryService.checkInventory(materialType, materialSectionType, 20.0, 10.0, 1000.0);

        assertFalse(result);
    }

    @Test
    void checkInventory_returnsTrueWhenInventoryHasEnoughQuantity() {
        doNothing().when(technicalSheetService).validateDimensions(materialSectionType, 20.0, 10.0);

        Inventory inventory = new Inventory();
        inventory.setId(1L);
        inventory.setMaterialType(materialType);
        inventory.setMaterialSectionType(materialSectionType);
        inventory.setDim1(20.0);
        inventory.setDim2(10.0);
        inventory.setQuantity(2000.0);

        when(inventoryRepository.findByMaterialTypeAndMaterialSectionTypeAndDim1AndDim2(
                eq(materialType), eq(materialSectionType), eq(20.0), eq(10.0)))
                .thenReturn(Optional.of(inventory));

        boolean result = inventoryService.checkInventory(materialType, materialSectionType, 20.0, 10.0, 1500.0);

        assertTrue(result);
    }

    @Test
    void checkInventory_returnsFalseWhenInventoryQuantityIsInsufficient() {
        doNothing().when(technicalSheetService).validateDimensions(materialSectionType, 20.0, 10.0);

        Inventory inventory = new Inventory();
        inventory.setId(2L);
        inventory.setMaterialType(materialType);
        inventory.setMaterialSectionType(materialSectionType);
        inventory.setDim1(20.0);
        inventory.setDim2(10.0);
        inventory.setQuantity(1000.0);

        when(inventoryRepository.findByMaterialTypeAndMaterialSectionTypeAndDim1AndDim2(
                eq(materialType), eq(materialSectionType), eq(20.0), eq(10.0)))
                .thenReturn(Optional.of(inventory));

        boolean result = inventoryService.checkInventory(materialType, materialSectionType, 20.0, 10.0, 1500.0);

        assertFalse(result);
    }
}
