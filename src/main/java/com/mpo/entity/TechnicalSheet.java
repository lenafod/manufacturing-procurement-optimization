package com.mpo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import jakarta.persistence.Table;

@Entity
@Data
@Table(name = "technical_sheet")
//tabela tehnickih listova koji se nalazi u okviru radnog naloga
//1 TL se moze nalaziti u n RN
public class TechnicalSheet {

    @Id
    private String id;

    private String sheetId;
    
    private String sheetVersion; //verzija po kojoj cu takodje filtrirati crteze

    @ManyToOne
    @JoinColumn(name = "work_order_id")
    private WorkOrder workOrder; // f.k

    private String positionName; // naziv ili sifra pozicije

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

    private Double prepLength;   // dužina pripremka
    
    private Double partMass;     // masa izratka

    private Double blankMass;    // masa pripremka
    
    private Double removedMass;  // masa koja se uklanja

}