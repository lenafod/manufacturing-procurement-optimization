package com.mpo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "work_order")
public class WorkOrder {

    @Id
    private String id; // proveriti u kom obliku je id radnog naloga
    
    @OneToMany(mappedBy = "workOrder")
    private List<TechnicalSheet> technicalSheets = new ArrayList<>();
}