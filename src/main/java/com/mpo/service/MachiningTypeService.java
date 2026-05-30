package com.mpo.service;

import org.springframework.stereotype.Service;

import com.mpo.entity.MachiningType;
import com.mpo.repository.MachiningTypeRepository;

import lombok.RequiredArgsConstructor;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MachiningTypeService {

    private final MachiningTypeRepository machiningTypeRepository;

    public List<MachiningType> getAll() {
        return machiningTypeRepository.findAll();
    }

    public MachiningType getById(Integer id) {
        return machiningTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("machining type with this id does not exist"));
    }
}
