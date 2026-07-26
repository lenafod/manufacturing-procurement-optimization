package com.mpo.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
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

import static com.mpo.enums.SectionShape.RECTANGULAR;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

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
        materialSectionType.setTypeName(RECTANGULAR);
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
        when(inventoryRepository.findByMaterialTypeAndMaterialSectionType(
                eq(materialType), eq(materialSectionType)))
                .thenReturn(Optional.empty());

        boolean result = inventoryService.checkInventory(materialType, materialSectionType, 1000.0);

        assertFalse(result);
    }

    @Test
    void checkInventory_returnsTrueWhenInventoryHasEnoughQuantity() {
        Inventory inventory = new Inventory();
        inventory.setId(1L);
        inventory.setMaterialType(materialType);
        inventory.setMaterialSectionType(materialSectionType);
        inventory.setQuantity(2000.0);

        when(inventoryRepository.findByMaterialTypeAndMaterialSectionType(
                eq(materialType), eq(materialSectionType)))
                .thenReturn(Optional.of(inventory));

        boolean result = inventoryService.checkInventory(materialType, materialSectionType, 1500.0);

        assertTrue(result);
    }

    @Test
    void checkInventory_returnsFalseWhenInventoryQuantityIsInsufficient() {
        Inventory inventory = new Inventory();
        inventory.setId(2L);
        inventory.setMaterialType(materialType);
        inventory.setMaterialSectionType(materialSectionType);
        inventory.setQuantity(1000.0);

        when(inventoryRepository.findByMaterialTypeAndMaterialSectionType(
                eq(materialType), eq(materialSectionType)))
                .thenReturn(Optional.of(inventory));

        boolean result = inventoryService.checkInventory(materialType, materialSectionType, 1500.0);

        assertFalse(result);
    }
}
