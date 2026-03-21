package com.mpo.service;

import org.springframework.stereotype.Service;

import com.mpo.entity.TechnicalProcessing;
import com.mpo.repository.TehnicalProcessingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TehnicalProcessingService {

    private final TehnicalProcessingRepository tehnicalProcessingRepository;

    public TechnicalProcessing getById(Integer id) {
        return tehnicalProcessingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("tehnical processing with this id does not exist"));
    }
}
