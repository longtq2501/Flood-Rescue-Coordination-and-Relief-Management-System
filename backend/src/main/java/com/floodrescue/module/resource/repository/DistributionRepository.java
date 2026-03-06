package com.floodrescue.module.resource.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.floodrescue.module.resource.domain.entity.Distribution;

@Repository
public interface DistributionRepository extends JpaRepository<Distribution, Long> {

    Page<Distribution> findByRecipientId(Long recipientId, Pageable pageable);

    Page<Distribution> findByRequestId(Long requestId, Pageable pageable);
}