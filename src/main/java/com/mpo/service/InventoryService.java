package com.mpo.service;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

import com.mpo.entity.Inventory;
import com.mpo.entity.MaterialSectionType;
import com.mpo.entity.MaterialType;
import com.mpo.exception.ResourceNotFoundException;
import com.mpo.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

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

    //provera lagera prema matrijalu i preseku (presek vec odredjuje dimenzije)
    public boolean checkInventory(MaterialType materialType, MaterialSectionType materialSectionType,
                                  Double requiredQuantity) {
        Inventory inventory = inventoryRepository
            .findByMaterialTypeAndMaterialSectionType(materialType, materialSectionType)
            .orElse(null);

        if (inventory == null) {
            return false;
        }

        return checkQuantity(requiredQuantity, inventory.getQuantity());

    }

    //poveca stanje lagera kad porudzbina stigne (PurchaseRequest -> DELIVERED); pravi novi red ako materijal+presek jos nema stanje
    public Inventory increaseQuantity(MaterialType materialType, MaterialSectionType materialSectionType, Double amount) {
        Inventory inventory = inventoryRepository
                .findByMaterialTypeAndMaterialSectionType(materialType, materialSectionType)
                .orElseGet(() -> {
                    Inventory newInventory = new Inventory();
                    newInventory.setMaterialType(materialType);
                    newInventory.setMaterialSectionType(materialSectionType);
                    newInventory.setQuantity(0.0);
                    return newInventory;
                });

        inventory.setQuantity(inventory.getQuantity() + amount);
        return inventoryRepository.save(inventory);
    }

}
