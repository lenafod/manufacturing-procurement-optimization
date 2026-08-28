package com.mpo.entity;

import com.mpo.enums.ProcurementInquiryStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "procurement_inquiry")
// upit dobavljacu da potvrdi stvarnu raspolozivu kolicinu materijala pre nego sto se pokrene
// optimizacija - odvojen je od PurchaseRequest jer ne predstavlja stvarnu porudzbinu, samo
// raspitivanje; kad stigne odgovor, confirmedQuantity se upisuje i u SupplierMaterial.availableQuantity
public class ProcurementInquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "technical_sheet_id")
    private TechnicalSheet technicalSheet;

    @ManyToOne
    @JoinColumn(name = "supplier_material_id")
    private SupplierMaterial supplierMaterial;

    private Double requestedQuantity; // potrebna kolicina u trenutku slanja upita
    private Double confirmedQuantity; // stvarna kolicina koju je dobavljac potvrdio (null dok ne odgovori)
    private Double confirmedPrice; // stvarna cena po jedinici koju je dobavljac potvrdio
    private Integer confirmedDeliveryTime; // stvaran rok isporuke (dana) koji je dobavljac potvrdio

    @Enumerated(EnumType.STRING)
    private ProcurementInquiryStatus status;

    private LocalDate sentAt;
    private LocalDate respondedAt;
}
