package com.floodrescue.resource.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.floodrescue.resource.domain.entity.Distribution;

@Repository
public interface DistributionRepository extends JpaRepository<Distribution, Long> {

    @Query("SELECT d FROM Distribution d LEFT JOIN FETCH d.items WHERE d.requestId = :requestId")
    Page<Distribution> findByRequestId(Long requestId, Pageable pageable);

    @Query("SELECT d FROM Distribution d LEFT JOIN FETCH d.items WHERE d.recipientId = :recipientId")
    Page<Distribution> findByRecipientId(Long recipientId, Pageable pageable);
}
