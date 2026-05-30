package com.mpo.controller;

import com.mpo.entity.TechnicalProcessing;
import com.mpo.service.TechnicalProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/technical-processings")
@RequiredArgsConstructor
public class TechnicalProcessingController {

    private final TechnicalProcessingService tehnicalProcessingService;

    @GetMapping
    public List<TechnicalProcessing> getAll() {
        return tehnicalProcessingService.getAll();
    }

    @GetMapping("/{id}")
    public TechnicalProcessing getById(@PathVariable Integer id) {
        return tehnicalProcessingService.getById(id);
    }
}