package com.mpo.service;

import org.springframework.stereotype.Service;

import com.mpo.entity.Supplier;
import com.mpo.exception.ResourceNotFoundException;
import com.mpo.repository.SupplierRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public List<Supplier> getAll() {
        return supplierRepository.findAll();
    }

    public Supplier getById(Integer id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("supplier with id " + id + " does not exist"));
    }

    public Supplier save(Supplier supplier) {
        return supplierRepository.save(supplier);
    }
}
