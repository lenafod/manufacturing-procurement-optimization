package com.mpo.service;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.mpo.entity.MaterialSectionType;
import com.mpo.entity.TechnicalSheet;
import com.mpo.entity.WorkOrder;
import com.mpo.exception.DuplicateResourceException;
import com.mpo.exception.ResourceNotFoundException;
import com.mpo.repository.WorkOrderRepository;

import jakarta.persistence.criteria.Join;
import lombok.RequiredArgsConstructor;
import java.util.List;
@Service
@RequiredArgsConstructor
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;

    public WorkOrder saveWorkOrder(WorkOrder workOrder) {
        if (workOrderRepository.existsById(workOrder.getId())) {
            throw new DuplicateResourceException("work order with id " + workOrder.getId() + " already exists");
        }
        return workOrderRepository.save(workOrder);
    }

    public List<WorkOrder> getAll() {
        return workOrderRepository.findAll();
    }

    // pretraga radnih naloga po vise nezavisnih kriterijuma - svi opcioni, kombinuju se sa AND.
    // id je pretraga po delu teksta (case-insensitive); materialTypeId i positionName svaki
    // dobija SVOJ join na technicalSheets, tako da nalog odgovara ako BILO KOJA pozicija ima taj
    // materijal I (nezavisno, moguce druga) pozicija odgovara tom nazivu - a ne samo ako JEDNA
    // ista pozicija zadovoljava oba uslova istovremeno
    public List<WorkOrder> search(String id, Integer materialTypeId, String positionName) {
        Specification<WorkOrder> spec = null;

        if (id != null && !id.isBlank()) {
            String pattern = "%" + id.toLowerCase() + "%";
            Specification<WorkOrder> idSpec = (root, query, cb) -> cb.like(cb.lower(root.get("id")), pattern);
            spec = (spec == null) ? idSpec : spec.and(idSpec);
        }

        if (materialTypeId != null) {
            Specification<WorkOrder> materialSpec = (root, query, cb) -> {
                query.distinct(true);
                Join<WorkOrder, TechnicalSheet> sheets = root.join("technicalSheets");
                return cb.equal(sheets.get("materialType").get("id"), materialTypeId);
            };
            spec = (spec == null) ? materialSpec : spec.and(materialSpec);
        }

        if (positionName != null && !positionName.isBlank()) {
            String pattern = "%" + positionName.toLowerCase() + "%";
            Specification<WorkOrder> positionSpec = (root, query, cb) -> {
                query.distinct(true);
                Join<WorkOrder, TechnicalSheet> sheets = root.join("technicalSheets");
                return cb.like(cb.lower(sheets.get("positionName")), pattern);
            };
            spec = (spec == null) ? positionSpec : spec.and(positionSpec);
        }

        return spec == null ? workOrderRepository.findAll() : workOrderRepository.findAll(spec);
    }

    public WorkOrder getById(String id) {
        return workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("work order not found: " + id));
    }
}