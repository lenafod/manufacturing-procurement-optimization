package com.mpo.dto;

import com.mpo.entity.PurchaseRequest;

import java.util.List;

// rezultat optimizacije nabavke za ceo radni nalog - razdvaja pozicije za koje je
// napravljen zahtev od onih koje su preskocene (i zasto), da front ne prikazuje
// istu poruku za "dovoljno na lageru", "vec postoji zahtev" i "nema ponuda"
public record OptimizationResult(List<PurchaseRequest> created, List<SkippedPosition> skipped) {

    public record SkippedPosition(String positionName, String reason) {
    }
}
