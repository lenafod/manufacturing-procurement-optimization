package com.mpo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import lombok.Data;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import java.time.LocalDate;

import lombok.RequiredArgsConstructor;

import com.mpo.enums.PurchaseRequestStatus;

@Entity
@Data
@Table(name = "purchase_request")
@RequiredArgsConstructor
public class PurchaseRequest {
    
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "technical_sheet_id")
    private TechnicalSheet technicalSheet;

    @ManyToOne
    @JoinColumn(name = "supplier_material_id")
    private SupplierMaterial supplierMaterial;

    private Double requiredQuantity;      // koliko mm treba naručiti
    private Double totalPrice;            // requiredQuantity × pricePerMm

    @Enumerated(EnumType.STRING)
    private PurchaseRequestStatus status;

    private LocalDate createdAt;
    private LocalDate expectedDeliveryDate;  // createdAt + deliveryDays od dobavljaca
    private LocalDate actualDeliveryDate;    // popunjava se kad roba stigne

}
