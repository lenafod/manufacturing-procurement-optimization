package com.mpo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@Data
@Table(name = "supplier_material")
public class SupplierMaterial {
//jedan dobavljac moze da prodaje vise razlicitih materijala,
// a jedan materijal moze da se nabavlja od vise dobavljaca
//ova tabela je za ponudu
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @ManyToOne
    @JoinColumn(name = "material_type_id")
    private MaterialType materialType;

    @ManyToOne
    @JoinColumn(name = "material_section_type_id")
    private MaterialSectionType materialSectionType;

    private Double pricePerUnit; //cena po jedinici mere
    private Integer deliveryTime; //vreme isporuke u danima
}
