package com.mpo.service;

import com.mpo.entity.ProcurementInquiry;
import com.mpo.entity.SupplierMaterial;
import com.mpo.entity.TechnicalSheet;
import com.mpo.enums.ProcurementInquiryStatus;
import com.mpo.exception.InvalidRequestException;
import com.mpo.exception.ResourceNotFoundException;
import com.mpo.repository.ProcurementInquiryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProcurementInquiryService {

    private final ProcurementInquiryRepository procurementInquiryRepository;
    private final TechnicalSheetService technicalSheetService;
    private final SupplierMaterialService supplierMaterialService;
    private final JavaMailSender mailSender;

    @Value("${procurement.mail.from}")
    private String fromAddress;

    public ProcurementInquiryService(ProcurementInquiryRepository procurementInquiryRepository,
                                      TechnicalSheetService technicalSheetService,
                                      SupplierMaterialService supplierMaterialService,
                                      JavaMailSender mailSender) {
        this.procurementInquiryRepository = procurementInquiryRepository;
        this.technicalSheetService = technicalSheetService;
        this.supplierMaterialService = supplierMaterialService;
        this.mailSender = mailSender;
    }

    public record InquiryEmailPreview(String to, String subject, String text) {
    }

    public List<ProcurementInquiry> getAll() {
        return procurementInquiryRepository.findAll();
    }

    // sadrzaj mejla pre slanja - koristi isti buildInquiryEmail kao stvarno slanje, tako da je
    // pregled garantovano identican onome sto ce zaista otici
    public InquiryEmailPreview previewInquiryEmail(String technicalSheetId, Integer supplierMaterialId) {
        TechnicalSheet technicalSheet = technicalSheetId != null ? technicalSheetService.getById(technicalSheetId) : null;
        Double neededQuantity = technicalSheet != null ? technicalSheet.getPrepLength() * technicalSheet.getQuantity() : null;
        SupplierMaterial offer = supplierMaterialService.getById(supplierMaterialId);

        return buildInquiryEmail(technicalSheet, offer, neededQuantity);
    }

    // svi dobavljaci koji uopste nose ovaj materijal/presek za datu poziciju, bez rangiranja -
    // korisnik sam bira kome od njih salje upit
    public List<SupplierMaterial> getCandidateOffers(String technicalSheetId) {
        TechnicalSheet technicalSheet = technicalSheetService.getById(technicalSheetId);
        return supplierMaterialService.findOffersForMaterial(technicalSheet.getMaterialType(), technicalSheet.getMaterialSectionType());
    }

    // technicalSheetId je opcion - upit se moze poslati vezan za konkretnu poziciju (iz "Upiti"
    // stranice, zna se tacna potrebna kolicina) ili nezavisno, direktno sa ponude dobavljaca
    // (iz "Dobavljači" stranice, samo se pita za trenutno stanje bez vezivanja za neku potrebu).
    // subjectOverride/textOverride su ono sto je korisnik izmenio u pregledu pre slanja - ako
    // nisu poslati (null/prazno), koristi se automatski generisan sadrzaj (buildInquiryEmail)
    public List<ProcurementInquiry> sendInquiries(String technicalSheetId, List<Integer> supplierMaterialIds,
                                                   String subjectOverride, String textOverride) {
        TechnicalSheet technicalSheet = technicalSheetId != null ? technicalSheetService.getById(technicalSheetId) : null;
        Double neededQuantity = technicalSheet != null ? technicalSheet.getPrepLength() * technicalSheet.getQuantity() : null;

        List<ProcurementInquiry> sentInquiries = new ArrayList<>();

        for (Integer supplierMaterialId : supplierMaterialIds) {
            SupplierMaterial offer = supplierMaterialService.getById(supplierMaterialId);
            InquiryEmailPreview defaultEmail = buildInquiryEmail(technicalSheet, offer, neededQuantity);

            String subject = subjectOverride != null && !subjectOverride.isBlank() ? subjectOverride : defaultEmail.subject();
            String text = textOverride != null && !textOverride.isBlank() ? textOverride : defaultEmail.text();

            ProcurementInquiry inquiry = new ProcurementInquiry();
            inquiry.setTechnicalSheet(technicalSheet);
            inquiry.setSupplierMaterial(offer);
            inquiry.setRequestedQuantity(neededQuantity);
            inquiry.setStatus(ProcurementInquiryStatus.POSLAT);
            inquiry.setSentAt(LocalDate.now());

            sendRawEmail(defaultEmail.to(), subject, text);

            sentInquiries.add(procurementInquiryRepository.save(inquiry));
        }

        return sentInquiries;
    }

    // odgovor dobavljaca nosi sve troje - cena i rok su isto tako nepouzdani kao i kolicina dok
    // se ne potvrde u razgovoru, ne samo kolicina. Sve troje se odjednom upisuje na ponudu.
    public ProcurementInquiry recordResponse(Long inquiryId, Double confirmedQuantity, Double confirmedPrice, Integer confirmedDeliveryTime) {
        ProcurementInquiry inquiry = procurementInquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("procurement inquiry with id " + inquiryId + " not found"));

        // sprecava da dupli/stale submit tiho prepise vec potvrdjen odgovor drugim brojevima
        if (inquiry.getStatus() == ProcurementInquiryStatus.ODGOVOREN) {
            throw new InvalidRequestException("inquiry with id " + inquiryId + " has already been answered");
        }

        inquiry.setConfirmedQuantity(confirmedQuantity);
        inquiry.setConfirmedPrice(confirmedPrice);
        inquiry.setConfirmedDeliveryTime(confirmedDeliveryTime);
        inquiry.setStatus(ProcurementInquiryStatus.ODGOVOREN);
        inquiry.setRespondedAt(LocalDate.now());

        SupplierMaterial updatedOffer = new SupplierMaterial();
        updatedOffer.setPricePerUnit(confirmedPrice);
        updatedOffer.setDeliveryTime(confirmedDeliveryTime);
        updatedOffer.setAvailableQuantity(confirmedQuantity);
        supplierMaterialService.updateSupplierMaterial(inquiry.getSupplierMaterial().getId(), updatedOffer);

        return procurementInquiryRepository.save(inquiry);
    }

    private void sendRawEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    private InquiryEmailPreview buildInquiryEmail(TechnicalSheet technicalSheet, SupplierMaterial offer, Double neededQuantity) {
        String positionLine = technicalSheet != null ? "Pozicija: " + technicalSheet.getPositionName() + "\n" : "";
        String quantityLine = neededQuantity != null ? "Potrebna količina: " + neededQuantity + "\n" : "";
        String subjectSuffix = technicalSheet != null ? ": " + technicalSheet.getPositionName() : "";

        String subject = "Upit za raspoloživu količinu" + subjectSuffix;
        String text =
                "Poštovani,\n\n" +
                "Molimo za informaciju o trenutno raspoloživoj količini sledećeg materijala:\n\n" +
                positionLine +
                "Materijal: " + offer.getMaterialType().getMaterialName() + "\n" +
                "Presek: " + offer.getMaterialSectionType().toDisplayString() + "\n" +
                quantityLine +
                "Poznata cena po jedinici: " + offer.getPricePerUnit() + "\n" +
                "Poznat rok isporuke: " + offer.getDeliveryTime() + " dana\n\n" +
                "Molimo potvrdite tačnu raspoloživu količinu u odgovoru na ovaj mejl.\n\n" +
                "Hvala,\nNabavka";

        return new InquiryEmailPreview(offer.getSupplier().getEmail(), subject, text);
    }
}
