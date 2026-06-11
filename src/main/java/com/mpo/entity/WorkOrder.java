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

    private Integer quantity; // broj komada

    @OneToMany(mappedBy = "workOrder")
    private List<TechnicalSheet> technicalSheets; 
}