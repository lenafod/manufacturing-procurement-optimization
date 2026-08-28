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
import java.util.ArrayList;
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
    // id je pretraga po delu teksta (case-insensitive); materialTypeId i positionName gledaju
    // da li BAR JEDNA pozicija (tehnicki list) u nalogu odgovara tom kriterijumu
    public List<WorkOrder> search(String id, Integer materialTypeId, String positionName) {
        Specification<WorkOrder> spec = null;

        if (id != null && !id.isBlank()) {
            String pattern = "%" + id.toLowerCase() + "%";
            Specification<WorkOrder> idSpec = (root, query, cb) -> cb.like(cb.lower(root.get("id")), pattern);
            spec = (spec == null) ? idSpec : spec.and(idSpec);
        }

        if (materialTypeId != null || (positionName != null && !positionName.isBlank())) {
            Specification<WorkOrder> sheetSpec = (root, query, cb) -> {
                query.distinct(true);
                Join<WorkOrder, TechnicalSheet> sheets = root.join("technicalSheets");
                List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

                if (materialTypeId != null) {
                    predicates.add(cb.equal(sheets.get("materialType").get("id"), materialTypeId));
                }
                if (positionName != null && !positionName.isBlank()) {
                    predicates.add(cb.like(cb.lower(sheets.get("positionName")), "%" + positionName.toLowerCase() + "%"));
                }

                return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
            };
            spec = (spec == null) ? sheetSpec : spec.and(sheetSpec);
        }

        return spec == null ? workOrderRepository.findAll() : workOrderRepository.findAll(spec);
    }

    public WorkOrder getById(String id) {
        return workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("work order not found: " + id));
    }
}