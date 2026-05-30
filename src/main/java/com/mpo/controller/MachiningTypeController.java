package com.mpo.controller;

import com.mpo.entity.MachiningType;
import com.mpo.service.MachiningTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/machining-types")
@RequiredArgsConstructor
public class MachiningTypeController {

    private final MachiningTypeService machiningTypeService;

    @GetMapping
    public List<MachiningType> getAll() {
        return machiningTypeService.getAll();
    }

    @GetMapping("/{id}")
    public MachiningType getById(@PathVariable Integer id) {
        return machiningTypeService.getById(id);
    }
}