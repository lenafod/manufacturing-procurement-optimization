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
//neki request mapping
@RequiredArgsConstructor
public class WorkOrderController {

}