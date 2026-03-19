package com.mpo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import jakarta.persistence.Table;

@Entity
@Data
@Table(name = "work_order")
public class WorkOrder {

    @Id
    private String id; // proveriti u kom obliku je id radnog naloga

    private String positionName; // naziv ili sifra pozicije
    private Integer quantity; // broj komada

    @ManyToOne
    @JoinColumn(name = "material_type_id")
    private MaterialType materialType; // f.k. iz tabele materialtype

    @ManyToOne
    @JoinColumn(name = "material_section_id")
    private MaterialSectionType materialSectionType; // f.k. iz tabele materialsection - oblik preseka

    ////////// ????????????????????????/
    private Integer materialSectionLength; // dimenzija preseka, u zavisnosti od MST
    // proveriti dal da bude 1 promenljiva

    private Double partLength; // duzina izratka
    private Double technicalAllowance; // tehnicki dodatak

    private Integer positionSurface; // povrsina pozicije ? sta god je to

    @ManyToOne
    @JoinColumn(name = "technical_processing_id")
    private TechnicalProcessing technicalProcessing; // moze i int, ako uzimam kljuc

    @ManyToOne
    @JoinColumn(name = "surface_protection_id")
    private SurfaceProtection surfaceProtection; // povrsinska zastita takodje sta god

    @ManyToOne
    @JoinColumn(name = "machining_type_id")
    private MachiningType machiningType; // sigurno cu imati PK

}