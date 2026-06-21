package com.mpo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mpo.entity.Inventory;
import com.mpo.entity.MaterialSectionType;
import com.mpo.entity.MaterialType;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByMaterialTypeAndMaterialSectionTypeAndDim1AndDim2(
            MaterialType materialType,
            MaterialSectionType materialSectionType,
            Double dim1,
            Double dim2);
}