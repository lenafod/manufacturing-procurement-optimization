package com.mpo.service;

import org.springframework.stereotype.Service;

import com.mpo.entity.MaterialSectionType;
import com.mpo.repository.MaterialSectionTypeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MaterialSectionTypeService {

    private final MaterialSectionTypeRepository materialSectionTypeRepository;

    public MaterialSectionType getById(Integer id) {
        return materialSectionTypeRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("material section type with this id does not exist"));
    }

}
