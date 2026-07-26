package com.mpo.service;

import org.springframework.stereotype.Service;

import com.mpo.entity.MaterialType;
import com.mpo.exception.ResourceNotFoundException;
import com.mpo.repository.MaterialTypeRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialTypeService {

    private final MaterialTypeRepository materialTypeRepository;

    public List<MaterialType> getAll() {
        return materialTypeRepository.findAll();
    }

    public MaterialType getById(Integer id) {
        return materialTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("material type with id " + id + " does not exist"));
    }

    public MaterialType getByMaterialTypeName(String materialTypeName) {
        return materialTypeRepository.findByMaterialName(materialTypeName)
                .orElseThrow(() -> new ResourceNotFoundException("material type with name " + materialTypeName + " does not exist"));
    }
}