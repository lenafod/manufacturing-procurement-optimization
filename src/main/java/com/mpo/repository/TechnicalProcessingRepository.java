package com.mpo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mpo.entity.TechnicalProcessing;

@Repository
public interface TechnicalProcessingRepository extends JpaRepository<TechnicalProcessing, Integer> {

}