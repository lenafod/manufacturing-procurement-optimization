package com.mpo.service;

import org.springframework.stereotype.Service;

import com.mpo.entity.MachiningType;
import com.mpo.repository.MachiningTypeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MachiningTypeService {

    private final MachiningTypeRepository machiningTypeRepository;

    public MachiningType getById(Integer id) {
        return machiningTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("machining type with this id does not exist"));
    }
}
