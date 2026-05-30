package com.mpo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mpo.entity.MaterialType;
import com.mpo.service.MaterialTypeService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/materialtype")
@RequiredArgsConstructor
public class MaterialTypeController {

    private final MaterialTypeService materialTypeService;

    @GetMapping
    public List<MaterialType> getAll() {
        return materialTypeService.getAll();
    }

    @GetMapping("/{id}")
    public MaterialType getById(Integer id) {
        return materialTypeService.getById(id);
    }

}
