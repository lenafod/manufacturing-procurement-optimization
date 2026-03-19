package com.mpo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import jakarta.persistence.Table;

@Entity
@Data
@Table(name = "surface_protection")
public class SurfaceProtection {

    @Id
    private Integer id;
}
