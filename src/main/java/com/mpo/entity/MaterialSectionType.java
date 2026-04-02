package com.mpo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import jakarta.persistence.Table;

@Entity
@Data
@Table(name = "material_section_type")
public class MaterialSectionType {

    @Id
    private Integer id;

    private String typeName; //tip preseka

    private Double dim1; //duzina preseka
    private Double dim2; //moze da postoji a ne mora
}