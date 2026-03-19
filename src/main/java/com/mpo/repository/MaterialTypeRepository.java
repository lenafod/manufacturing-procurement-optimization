package com.mpo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mpo.entity.MaterialType;

@Repository
public interface MaterialTypeRepository extends JpaRepository<MaterialType, Integer> {

}