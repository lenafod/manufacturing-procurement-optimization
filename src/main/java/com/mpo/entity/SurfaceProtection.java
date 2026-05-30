package com.mpo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.Getter;
import jakarta.persistence.Table;

@Entity
@Data
@Getter
@Table(name = "surface_protection")
public class SurfaceProtection {

    @Id
    private Integer id;

    private String name;
}
