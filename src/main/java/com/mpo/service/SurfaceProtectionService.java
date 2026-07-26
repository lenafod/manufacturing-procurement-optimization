package com.mpo.service;

import org.springframework.stereotype.Service;

import com.mpo.entity.SurfaceProtection;
import com.mpo.exception.ResourceNotFoundException;
import com.mpo.repository.SurfaceProtectionRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SurfaceProtectionService {

    private final SurfaceProtectionRepository surfaceProtectionRepository;

    public List<SurfaceProtection> getAll() {
        return surfaceProtectionRepository.findAll();
    }

    public SurfaceProtection getById(Integer id) {
        return surfaceProtectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("surface protection with this id does not exist"));
    }
}
