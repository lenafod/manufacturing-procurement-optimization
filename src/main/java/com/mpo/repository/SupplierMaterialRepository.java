package com.mpo.repository;

import com.mpo.entity.SupplierMaterial;
import com.mpo.entity.MaterialType;
import com.mpo.entity.MaterialSectionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SupplierMaterialRepository extends JpaRepository<SupplierMaterial, Integer> {
    List<SupplierMaterial> findBySupplierId(Integer supplierId);
    List<SupplierMaterial> findByMaterialTypeAndMaterialSectionType(
        MaterialType materialType,
        MaterialSectionType materialSectionType
    );
    List<SupplierMaterial> findByMaterialTypeAndMaterialSectionTypeAndDim1AndDim2(
        MaterialType materialType,
        MaterialSectionType materialSectionType,
        Double dim1,
        Double dim2
    );
}