package com.floodrescue.resource.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.floodrescue.resource.domain.entity.Distribution;

@Repository
public interface DistributionRepository extends JpaRepository<Distribution, Long> {

    @Query(value = "SELECT d FROM Distribution d WHERE d.requestId = :requestId", countQuery = "SELECT COUNT(d) FROM Distribution d WHERE d.requestId = :requestId")
    Page<Distribution> findByRequestId(@Param("requestId") Long requestId, Pageable pageable);

    @Query(value = "SELECT d FROM Distribution d WHERE d.recipientId = :recipientId", countQuery = "SELECT COUNT(d) FROM Distribution d WHERE d.recipientId = :recipientId")
    Page<Distribution> findByRecipientId(@Param("recipientId") Long recipientId, Pageable pageable);

    @Query("SELECT d FROM Distribution d LEFT JOIN FETCH d.items WHERE d IN :distributions")
    List<Distribution> fetchWithItems(@Param("distributions") List<Distribution> distributions);
}