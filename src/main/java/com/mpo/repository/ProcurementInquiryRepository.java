package com.mpo.repository;

import com.mpo.entity.ProcurementInquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcurementInquiryRepository extends JpaRepository<ProcurementInquiry, Long> {
}
