package com.mpo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import jakarta.persistence.Table;

@Entity
@Data
@Table(name = "technical_processing")
public class TechnicalProcessing {

    @Id
    private Integer id;
}
