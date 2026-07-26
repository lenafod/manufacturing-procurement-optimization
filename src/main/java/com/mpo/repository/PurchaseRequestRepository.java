package com.mpo.repository;

import com.mpo.entity.PurchaseRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.mpo.enums.PurchaseRequestStatus;
import java.util.List;

@Repository
public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long> {

    List<PurchaseRequest> findByStatus(PurchaseRequestStatus status);
}