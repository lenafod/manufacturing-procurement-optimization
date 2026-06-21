package com.mpo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mpo.entity.MaterialSectionType;

@Repository
public interface MaterialSectionTypeRepository extends JpaRepository<MaterialSectionType, Integer> {

    java.util.Optional<MaterialSectionType> findByTypeName(String typeName);
}