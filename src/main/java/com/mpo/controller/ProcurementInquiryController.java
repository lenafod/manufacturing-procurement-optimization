package com.mpo.controller;

import com.mpo.entity.ProcurementInquiry;
import com.mpo.entity.SupplierMaterial;
import com.mpo.service.ProcurementInquiryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/procurement-inquiries")
public class ProcurementInquiryController {

    private final ProcurementInquiryService procurementInquiryService;

    public ProcurementInquiryController(ProcurementInquiryService procurementInquiryService) {
        this.procurementInquiryService = procurementInquiryService;
    }

    @GetMapping
    public List<ProcurementInquiry> getAll() {
        return procurementInquiryService.getAll();
    }

    @GetMapping("/candidates/{technicalSheetId}")
    public List<SupplierMaterial> getCandidateOffers(@PathVariable String technicalSheetId) {
        return procurementInquiryService.getCandidateOffers(technicalSheetId);
    }

    @GetMapping("/preview")
    public ProcurementInquiryService.InquiryEmailPreview previewInquiryEmail(
            @RequestParam(required = false) String technicalSheetId,
            @RequestParam Integer supplierMaterialId) {
        return procurementInquiryService.previewInquiryEmail(technicalSheetId, supplierMaterialId);
    }

    public record SendInquiriesRequest(String technicalSheetId, List<Integer> supplierMaterialIds, String subject, String text) {
    }

    @PostMapping
    public List<ProcurementInquiry> sendInquiries(@RequestBody SendInquiriesRequest request) {
        return procurementInquiryService.sendInquiries(
                request.technicalSheetId(), request.supplierMaterialIds(), request.subject(), request.text());
    }

    @PatchMapping("/{id}/respond")
    public ProcurementInquiry recordResponse(@PathVariable Long id, @RequestParam Double confirmedQuantity) {
        return procurementInquiryService.recordResponse(id, confirmedQuantity);
    }
}
