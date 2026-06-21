package com.mpo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

import com.mpo.entity.MaterialType;

@Entity
@Data
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "material_type_id")
    private MaterialType materialType;

    @ManyToOne
    @JoinColumn(name = "material_section_id")
    private MaterialSectionType materialSectionType;

    private double dim1; //required
    private double dim2; //opciono, zavisi od oblika preseka

    //quantity == totalLengthAvailable
    //odnosno ukupna duzina materijala u magacinu, zavisi od jedinice mere
    private Double quantity; 
}