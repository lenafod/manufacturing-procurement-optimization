package com.mpo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "material_type")
public class MaterialType {

    @Id
    private Integer id;

    private String materialName;
}
