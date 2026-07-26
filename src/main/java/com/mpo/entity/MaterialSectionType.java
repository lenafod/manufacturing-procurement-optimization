package com.mpo.entity;

import com.mpo.enums.SectionShape;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Id;
import lombok.Data;
import jakarta.persistence.Table;
@Entity
@Data
@Table(name = "material_section_type")
public class MaterialSectionType {

    @Id
    private Integer id;

    @Enumerated(EnumType.STRING)
    private SectionShape typeName; //tip preseka

    private Double dim1; //duzina preseka
    private Double dim2; //moze da postoji a ne mora

    private Boolean usesDim2; //ovim se front bavi
}