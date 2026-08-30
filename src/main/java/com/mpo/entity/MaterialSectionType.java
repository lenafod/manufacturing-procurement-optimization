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
    private SectionShape typeName; 

    private Double dim1;
    private Double dim2;

    private Boolean usesDim2;

    // npr. "Okrugli 30 mm" ili "Pravougaoni 40x20 mm" - deljeno izmedju svih mesta koja
    // formatiraju presek za tekst mejla (upiti, porudzbine)
    public String toDisplayString() {
        String dims = Boolean.TRUE.equals(usesDim2) ? dim1 + "x" + dim2 : String.valueOf(dim1);
        return typeName.getDisplayName() + " " + dims + " mm";
    }
}