package com.mpo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.mpo.entity.TechnicalSheet;

@Repository
public interface TechnicalSheetRepository extends JpaRepository<TechnicalSheet, String>, JpaSpecificationExecutor<TechnicalSheet> {

}