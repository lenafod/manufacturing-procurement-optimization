package com.mpo.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import lombok.NonNull;

@Entity
public class WorkOrder {
    
    @Id
    String id; //proveriti u kom obliku je id radnog naloga

    String positionName; //naziv ili sifra pozicije

    Integer quantity; //broj komada

    String materialType; //f.k. iz tabele materialtype

    String materialSectionType; //f.k. iz tabele materialsection - oblik preseka

    Integer materialSectionLength; // dimenzija preseka, u zavisnosti od MST 
                                   // proveriti dal da bude 1 promenljiva

    Integer partLength;

}
