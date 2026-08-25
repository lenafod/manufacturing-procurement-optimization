package com.mpo.dto;

import com.mpo.entity.PurchaseRequest;

import java.util.List;

// rezultat optimizacije nabavke za ceo radni nalog - razdvaja pozicije za koje je
// napravljen zahtev od onih koje su preskocene (i zasto), da front ne prikazuje
// istu poruku za "dovoljno na lageru", "vec postoji zahtev" i "nema ponuda".
// jedna pozicija moze da proizvede vise PurchaseRequest redova u created (jedan po dobavljacu
// ako se potrebna kolicina deli izmedju vise ponuda); partial belezi pozicije kod kojih ni
// kombinovana raspoloziva kolicina svih dobavljaca nije dovoljna da pokrije celu potrebu
public record OptimizationResult(List<PurchaseRequest> created, List<PartialFulfillment> partial, List<SkippedPosition> skipped) {

    public record SkippedPosition(String positionName, String reason) {
    }

    public record PartialFulfillment(String positionName, double missingQuantity) {
    }
}
