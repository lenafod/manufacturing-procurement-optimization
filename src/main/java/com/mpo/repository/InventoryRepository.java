package com.mpo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mpo.entity.Inventory;
import com.mpo.entity.MaterialSectionType;
import com.mpo.entity.MaterialType;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByMaterialTypeAndMaterialSectionType(
            MaterialType materialType,
            MaterialSectionType materialSectionType);
}