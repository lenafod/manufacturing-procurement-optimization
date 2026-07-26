package com.mpo.service;

import org.springframework.stereotype.Service;

import com.mpo.entity.MaterialSectionType;
import com.mpo.exception.ResourceNotFoundException;
import com.mpo.repository.MaterialSectionTypeRepository;

import lombok.RequiredArgsConstructor;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialSectionTypeService {

    private final MaterialSectionTypeRepository materialSectionTypeRepository;

    public List<MaterialSectionType> getAll() {
        return materialSectionTypeRepository.findAll();
    }

    public MaterialSectionType getById(Integer id) {
        return materialSectionTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("material section type with this id does not exist"));
    }

    public MaterialSectionType getByTypeName(String typeName) {
        return materialSectionTypeRepository.findByTypeName(typeName)
                .orElseThrow(() -> new ResourceNotFoundException("material section type with this name does not exist"));
    }

}
