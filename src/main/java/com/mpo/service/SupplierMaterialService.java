package com.mpo.service;

import org.springframework.stereotype.Service;
import com.mpo.entity.MaterialSectionType;
import com.mpo.entity.MaterialType;
import com.mpo.entity.SupplierMaterial;
import com.mpo.exception.InvalidRequestException;
import com.mpo.exception.ResourceNotFoundException;
import com.mpo.repository.SupplierMaterialRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class SupplierMaterialService {

    private final SupplierMaterialRepository supplierMaterialRepository;

    public SupplierMaterialService(SupplierMaterialRepository supplierMaterialRepository) {
        this.supplierMaterialRepository = supplierMaterialRepository;
    }

    public List<SupplierMaterial> getAll() {
        return supplierMaterialRepository.findAll();
    }

    public SupplierMaterial getById(Integer id) {
        return supplierMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("supplier material with id " + id + " does not exist"));
    }

    public SupplierMaterial saveSupplierMaterial(SupplierMaterial supplierMaterial) {
        return supplierMaterialRepository.save(supplierMaterial);
    }

    // menja samo uslove ponude (cena, rok, raspoloziva kolicina) - dobavljac/materijal/presek
    // definisu identitet ponude i ne mogu da se menjaju posle kreiranja
    public SupplierMaterial updateSupplierMaterial(Integer id, SupplierMaterial updatedOffer) {
        SupplierMaterial existing = getById(id);
        existing.setPricePerUnit(updatedOffer.getPricePerUnit());
        existing.setDeliveryTime(updatedOffer.getDeliveryTime());
        existing.setAvailableQuantity(updatedOffer.getAvailableQuantity());
        return supplierMaterialRepository.save(existing);
    }

    //sve ponude za dati materijal i presek (presek vec odredjuje dimenzije)
    public List<SupplierMaterial> findOffersForMaterial(MaterialType materialType, MaterialSectionType materialSectionType) {
        return supplierMaterialRepository.findByMaterialTypeAndMaterialSectionType(materialType, materialSectionType);
    }

    private double normalizeCriteria(SupplierMaterial offer,
                                    Double maxPrice,
                                    Double maxDeliveryTime,
                                    Double w1, Double w2) {
        double normalizedPrice = offer.getPricePerUnit() / maxPrice;
        double normalizedDeliveryTime = offer.getDeliveryTime() / maxDeliveryTime;

        return w1 * normalizedPrice + w2 * normalizedDeliveryTime;
    }

    public SupplierMaterial findOptimal(List<SupplierMaterial> offers,
                                        Double weightPrice, Double weightDeliveryTime) {

        if (offers == null || offers.isEmpty()) {
            throw new ResourceNotFoundException("no offers available");
        }

        Double maxPrice = offers.stream()
                                .mapToDouble(SupplierMaterial::getPricePerUnit)
                                .max()
                                .orElse(0.0);

        Double maxDeliveryTime = offers.stream()
                                .mapToDouble(SupplierMaterial::getDeliveryTime)
                                .max()
                                .orElse(0.0);

        if (maxPrice == 0 || maxDeliveryTime == 0) {
            throw new InvalidRequestException("maxPrice and maxDeliveryTime must be greater than 0");
        }

        SupplierMaterial optimalOffer = null;
        double bestScore = Double.MAX_VALUE;

        for (SupplierMaterial offer : offers) {
            double score = normalizeCriteria(offer, maxPrice, maxDeliveryTime, weightPrice, weightDeliveryTime);
            if (score < bestScore) {
                bestScore = score;
                optimalOffer = offer;
            }
        }

        return optimalOffer;

    }

    // deo potrebne kolicine dodeljen jednoj ponudi u okviru alokacije na vise dobavljaca
    public record Allocation(SupplierMaterial offer, double quantity) {
    }

    // resava kontinualni (fractional knapsack) problem: rangira ponude po istom pondersanom
    // skoru kao findOptimal, pa ih redom puni najjeftiniju-prvu dok se ne pokrije requiredQuantity
    // ili ne ponestane raspolozivih ponuda - ovaj pohlepni pristup je dokazivo LP-optimalan za
    // ovu strukturu ogranicenja (jedno zajednicko ogranicenje pokrivenosti + gornja granica po ponudi).
    //
    // reservedQuantities: kolicina vec "rezervisana" na ponudi u okviru tekuceg poziva (kljuc =
    // supplierMaterial id), a koja jos nije upisana u bazu - koristi dry-run pregled (vidi
    // ProcurementOptimizationService.previewProcurementForWorkOrder) da vise pozicija u istom
    // pregledu ne racuna istu raspolozivu kolicinu dva puta. Za stvarno izvrsavanje se prosledjuje
    // prazna mapa po pozicionoj bazi jer se raspoloziva kolicina odmah upisuje u bazu.
    public List<Allocation> allocate(List<SupplierMaterial> offers, double requiredQuantity,
                                      Double weightPrice, Double weightDeliveryTime,
                                      Map<Integer, Double> reservedQuantities) {

        if (offers == null || offers.isEmpty()) {
            throw new ResourceNotFoundException("no offers available");
        }

        List<SupplierMaterial> usableOffers = offers.stream()
                .filter(o -> effectiveAvailableQuantity(o, reservedQuantities) > 0)
                .toList();

        if (usableOffers.isEmpty()) {
            return List.of();
        }

        Double maxPrice = usableOffers.stream()
                                .mapToDouble(SupplierMaterial::getPricePerUnit)
                                .max()
                                .orElse(0.0);

        Double maxDeliveryTime = usableOffers.stream()
                                .mapToDouble(SupplierMaterial::getDeliveryTime)
                                .max()
                                .orElse(0.0);

        if (maxPrice == 0 || maxDeliveryTime == 0) {
            throw new InvalidRequestException("maxPrice and maxDeliveryTime must be greater than 0");
        }

        List<SupplierMaterial> sortedByScore = usableOffers.stream()
                .sorted(Comparator.comparingDouble(o -> normalizeCriteria(o, maxPrice, maxDeliveryTime, weightPrice, weightDeliveryTime)))
                .toList();

        List<Allocation> allocations = new ArrayList<>();
        double remaining = requiredQuantity;

        for (SupplierMaterial offer : sortedByScore) {
            if (remaining <= 0) {
                break;
            }
            double take = Math.min(effectiveAvailableQuantity(offer, reservedQuantities), remaining);
            allocations.add(new Allocation(offer, take));
            remaining -= take;
            reservedQuantities.merge(offer.getId(), take, Double::sum);
        }

        return allocations;
    }

    private double effectiveAvailableQuantity(SupplierMaterial offer, Map<Integer, Double> reservedQuantities) {
        double base = offer.getAvailableQuantity() != null ? offer.getAvailableQuantity() : 0.0;
        double reserved = reservedQuantities.getOrDefault(offer.getId(), 0.0);
        return base - reserved;
    }

    public void decreaseAvailableQuantity(SupplierMaterial offer, double quantity) {
        double current = offer.getAvailableQuantity() != null ? offer.getAvailableQuantity() : 0.0;
        offer.setAvailableQuantity(current - quantity);
        supplierMaterialRepository.save(offer);
    }

    public void increaseAvailableQuantity(SupplierMaterial offer, double quantity) {
        double current = offer.getAvailableQuantity() != null ? offer.getAvailableQuantity() : 0.0;
        offer.setAvailableQuantity(current + quantity);
        supplierMaterialRepository.save(offer);
    }
}
