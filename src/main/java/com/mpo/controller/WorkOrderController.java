package com.mpo.controller;

import com.mpo.entity.MachiningType;
import com.mpo.entity.MaterialSectionType;
import com.mpo.entity.MaterialType;
import com.mpo.entity.SurfaceProtection;
import com.mpo.entity.TechnicalProcessing;
import com.mpo.entity.WorkOrder;
import com.mpo.service.WorkOrderService;
import com.mpo.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import java.io.IOException;

@RestController
@RequestMapping("/api/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;
    private final PdfService pdfService;

    @GetMapping
    public List<WorkOrder> getAll() {
        return workOrderService.getAll();
    }

    @GetMapping("/test-pdf")
    public ResponseEntity<byte[]> testPdf() throws IOException {
        // mokovani podaci za test
        MaterialType material = new MaterialType();
        material.setMaterialName("Celik S235");
        material.setDensity(7.85);

        MaterialSectionType section = new MaterialSectionType();
        section.setTypeName("ROUND");
        section.setDim1(30.0);
        section.setDim2(null);

        TechnicalProcessing processing = new TechnicalProcessing();
        processing.setName("Kaljenje");

        SurfaceProtection protection = new SurfaceProtection();
        protection.setName("Lakiranje");

        MachiningType machining = new MachiningType();
        machining.setName("Struganje");

        WorkOrder workOrder = new WorkOrder();
        workOrder.setId("RN-2025-001");
        workOrder.setPositionName("Osovina-A1");
        workOrder.setQuantity(5);
        workOrder.setMaterialType(material);
        workOrder.setMaterialSectionType(section);
        workOrder.setPartLength(250.0);
        workOrder.setTechnicalAllowance(10.0);
        workOrder.setPrepLength(260.0);
        workOrder.setPartMass(385.0);
        workOrder.setBlankMass(400.0);
        workOrder.setRemovedMass(15.0);
        workOrder.setTechnicalProcessing(processing);
        workOrder.setSurfaceProtection(protection);
        workOrder.setMachiningType(machining);

        byte[] pdf = pdfService.generateWorkOrderPdf(workOrder);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=test-nalog.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/{id}")
    public WorkOrder getById(@PathVariable String id) {
        return workOrderService.getById(id);
    }

    @PostMapping
    public WorkOrder save(@RequestBody WorkOrder workOrder) {
        return workOrderService.saveWorkOrder(workOrder);
    }

}