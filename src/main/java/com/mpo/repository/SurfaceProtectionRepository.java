package com.mpo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mpo.entity.SurfaceProtection;

@Repository
public interface SurfaceProtectionRepository extends JpaRepository<SurfaceProtection, Integer> {

}
