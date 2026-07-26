package com.mpo.service;

import org.springframework.stereotype.Service;
import com.mpo.entity.MaterialSectionType;
import com.mpo.entity.MaterialType;
import com.mpo.entity.SupplierMaterial;
import com.mpo.exception.ResourceNotFoundException;
import com.mpo.repository.SupplierMaterialRepository;
import java.util.List;

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
}
