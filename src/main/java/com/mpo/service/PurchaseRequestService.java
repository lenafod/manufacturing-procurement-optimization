package com.mpo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.mpo.entity.MaterialSectionType;
import com.mpo.entity.PurchaseRequest;
import com.mpo.enums.PurchaseRequestStatus;
import com.mpo.exception.InvalidRequestException;
import com.mpo.exception.ResourceNotFoundException;
import com.mpo.repository.PurchaseRequestRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class PurchaseRequestService {

    private final PurchaseRequestRepository purchaseRequestRepository;
    private final InventoryService inventoryService;
    private final SupplierMaterialService supplierMaterialService;
    private final JavaMailSender mailSender;

    @Value("${procurement.mail.from}")
    private String fromAddress;

    public PurchaseRequestService(PurchaseRequestRepository purchaseRequestRepository, InventoryService inventoryService,
                                   SupplierMaterialService supplierMaterialService, JavaMailSender mailSender) {
        this.purchaseRequestRepository = purchaseRequestRepository;
        this.inventoryService = inventoryService;
        this.supplierMaterialService = supplierMaterialService;
        this.mailSender = mailSender;
    }

    public List<PurchaseRequest> getPurchaseRequestsByStatus(PurchaseRequestStatus status) {
        return purchaseRequestRepository.findByStatus(status);
    }

    public List<PurchaseRequest> getAllPurchaseRequests() {
        return purchaseRequestRepository.findAll();
    }

    public List<PurchaseRequest> getPurchaseRequestsByWorkOrderId(String workOrderId) {
        return purchaseRequestRepository.findByTechnicalSheet_WorkOrder_Id(workOrderId);
    }

    public List<PurchaseRequest> getOverduePurchaseRequests() {
        List<PurchaseRequestStatus> excludedStatuses = List.of(PurchaseRequestStatus.DELIVERED, PurchaseRequestStatus.CANCELED);
        return purchaseRequestRepository.findByStatusNotInAndExpectedDeliveryDateBefore(excludedStatuses, LocalDate.now());
    }

    public PurchaseRequest getPurchaseRequestById(Long id) {
        return purchaseRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("purchase request with id " + id + " not found"));
    }

    public PurchaseRequest save(PurchaseRequest purchaseRequest) {
        return purchaseRequestRepository.save(purchaseRequest);
    }

    // sprecava duplirane zahteve - ako vec postoji nezavrsen zahtev za ovu poziciju, ne pravi se novi
    public boolean hasActiveRequestForTechnicalSheet(String technicalSheetId) {
        List<PurchaseRequestStatus> excludedStatuses = List.of(PurchaseRequestStatus.DELIVERED, PurchaseRequestStatus.CANCELED);
        return purchaseRequestRepository.existsByTechnicalSheet_IdAndStatusNotIn(technicalSheetId, excludedStatuses);
    }

    public PurchaseRequest updateStatus(Long purchaseRequestId, PurchaseRequestStatus newStatus) {
        PurchaseRequest purchaseRequest = purchaseRequestRepository.findById(purchaseRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("purchase request with id " + purchaseRequestId + " not found"));
        validateStatusChange(purchaseRequest, newStatus);
        purchaseRequest.setStatus(newStatus);

        if(newStatus == PurchaseRequestStatus.DELIVERED) {
            purchaseRequest.setActualDeliveryDate(LocalDate.now());
            inventoryService.increaseQuantity(
                    purchaseRequest.getTechnicalSheet().getMaterialType(),
                    purchaseRequest.getTechnicalSheet().getMaterialSectionType(),
                    purchaseRequest.getRequiredQuantity()
            );
        }

        // otkazivanje vraca rezervisanu kolicinu nazad dobavljacu (oduzeta je od availableQuantity kad je zahtev napravljen)
        if (newStatus == PurchaseRequestStatus.CANCELED) {
            supplierMaterialService.increaseAvailableQuantity(
                    purchaseRequest.getSupplierMaterial(),
                    purchaseRequest.getRequiredQuantity()
            );
        }

        // CREATED->SENT je trenutak kad porudzbina stvarno ide dobavljacu - do sada je "kreiran"
        // bio samo interna odluka (napravljena u Optimizaciji), mejl se salje tek na "Posalji"
        if (newStatus == PurchaseRequestStatus.SENT) {
            sendOrderEmail(purchaseRequest);
        }

        return purchaseRequestRepository.save(purchaseRequest);
    }

    private void sendOrderEmail(PurchaseRequest purchaseRequest) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(purchaseRequest.getSupplierMaterial().getSupplier().getEmail());
        message.setSubject("Porudžbina — " + purchaseRequest.getTechnicalSheet().getPositionName());
        message.setText(
                "Poštovani,\n\n" +
                "Ovim putem Vam šaljemo porudžbinu:\n\n" +
                "Pozicija: " + purchaseRequest.getTechnicalSheet().getPositionName() + "\n" +
                "Materijal: " + purchaseRequest.getSupplierMaterial().getMaterialType().getMaterialName() + "\n" +
                "Presek: " + formatSection(purchaseRequest.getSupplierMaterial().getMaterialSectionType()) + "\n" +
                "Količina: " + purchaseRequest.getRequiredQuantity() + " mm\n" +
                "Cena po jedinici: " + purchaseRequest.getSupplierMaterial().getPricePerUnit() + "\n" +
                "Ukupna cena: " + purchaseRequest.getTotalPrice() + "\n" +
                "Očekivan datum isporuke: " + purchaseRequest.getExpectedDeliveryDate() + "\n\n" +
                "Molimo potvrdite prijem porudžbine.\n\n" +
                "Hvala,\nNabavka"
        );
        mailSender.send(message);
    }

    private String formatSection(MaterialSectionType sectionType) {
        String dims = Boolean.TRUE.equals(sectionType.getUsesDim2())
                ? sectionType.getDim1() + "x" + sectionType.getDim2()
                : String.valueOf(sectionType.getDim1());
        return sectionType.getTypeName().getDisplayName() + " " + dims + " mm";
    }

    private void validateStatusChange(PurchaseRequest purchaseRequest, PurchaseRequestStatus newStatus) {
        PurchaseRequestStatus currentStatus = purchaseRequest.getStatus();

        if (currentStatus == PurchaseRequestStatus.CANCELED) {
            if (newStatus != PurchaseRequestStatus.CANCELED) {
                throw new InvalidRequestException("Cannot change status of a canceled purchase request");
            }
            return;
        }

        if (newStatus == PurchaseRequestStatus.CANCELED) {
            return;
        }

        List<PurchaseRequestStatus> linearFlow = List.of(
                PurchaseRequestStatus.CREATED,
                PurchaseRequestStatus.SENT,
                PurchaseRequestStatus.IN_DELIVERY,
                PurchaseRequestStatus.DELIVERED
        );

        if (linearFlow.indexOf(newStatus) < linearFlow.indexOf(currentStatus)) {
            throw new InvalidRequestException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }
    }
}
