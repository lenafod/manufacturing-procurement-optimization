package com.mpo.service;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

import com.mpo.entity.Inventory;
import com.mpo.entity.MaterialSectionType;
import com.mpo.entity.MaterialType;
import com.mpo.exception.ResourceNotFoundException;
import com.mpo.repository.InventoryRepository;
import com.mpo.service.TechnicalSheetService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final TechnicalSheetService technicalSheetService;

    public Inventory save(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    public Inventory getById(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory with this id does not exist"));
    }

    public List<Inventory> getAll() {
        return inventoryRepository.findAll();
    }

    //preplengt je uvek u mm
    public boolean checkQuantity(Double prepLength, Double availableQuantity) {
        return prepLength <= availableQuantity;
    }

    //provera lagera prema matrijalu, dimenzijama, preseku
    public boolean checkInventory(MaterialType materialType, MaterialSectionType materialSectionType,
                                  Double dim1, Double dim2, Double requiredQuantity) {
        technicalSheetService.validateDimensions(materialSectionType, dim1, dim2);

       Inventory inventory = inventoryRepository
            .findByMaterialTypeAndMaterialSectionTypeAndDim1AndDim2(materialType, materialSectionType, dim1, dim2)
            .orElse(null);

        if (inventory == null) {
            return false; 
        }

        return checkQuantity(requiredQuantity, inventory.getQuantity());
                     
    }

}
