package com.mpo.service;

import org.springframework.stereotype.Service;

import com.mpo.entity.TechnicalProcessing;
import com.mpo.exception.ResourceNotFoundException;
import com.mpo.repository.TechnicalProcessingRepository;

import lombok.RequiredArgsConstructor;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TechnicalProcessingService {

    private final TechnicalProcessingRepository technicalProcessingRepository;

    public List<TechnicalProcessing> getAll() {
        return technicalProcessingRepository.findAll();
    }

    public TechnicalProcessing getById(Integer id) {
        return technicalProcessingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("tehnical processing with this id does not exist"));
    }
}
