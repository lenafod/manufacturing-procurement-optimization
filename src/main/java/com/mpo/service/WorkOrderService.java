package com.mpo.service;

import org.springframework.stereotype.Service;

import com.mpo.entity.MaterialSectionType;

@Service
public class WorkOrderService {

    private Double prepLength;
    private Double prepMass;

    private void calculatePrepLength(Double partLength, Double technicalAllowance) {
        this.prepLength = partLength + technicalAllowance;
    }
}
