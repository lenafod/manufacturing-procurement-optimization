package com.mpo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mpo.entity.MachiningType;

@Repository
public interface MachiningTypeRepository extends JpaRepository<MachiningType, Integer>{

    
} 