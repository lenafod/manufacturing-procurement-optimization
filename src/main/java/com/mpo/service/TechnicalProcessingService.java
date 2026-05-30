package com.mpo.service;

import org.springframework.stereotype.Service;

import com.mpo.entity.TechnicalProcessing;
import com.mpo.repository.TehnicalProcessingRepository;

import lombok.RequiredArgsConstructor;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TechnicalProcessingService {

    private final TehnicalProcessingRepository tehnicalProcessingRepository;

    public List<TechnicalProcessing> getAll() {
        return tehnicalProcessingRepository.findAll();
    }

    public TechnicalProcessing getById(Integer id) {
        return tehnicalProcessingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("tehnical processing with this id does not exist"));
    }
}
