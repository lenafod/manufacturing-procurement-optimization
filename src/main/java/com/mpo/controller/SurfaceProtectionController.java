package com.mpo.controller;

import com.mpo.entity.SurfaceProtection;
import com.mpo.service.SurfaceProtectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/surface-protections")
@RequiredArgsConstructor
public class SurfaceProtectionController {

    private final SurfaceProtectionService surfaceProtectionService;

    @GetMapping
    public List<SurfaceProtection> getAll() {
        return surfaceProtectionService.getAll();
    }

    @GetMapping("/{id}")
    public SurfaceProtection getById(@PathVariable Integer id) {
        return surfaceProtectionService.getById(id);
    }
}