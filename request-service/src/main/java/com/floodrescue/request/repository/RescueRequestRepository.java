package com.floodrescue.request.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.floodrescue.request.domain.entity.RescueRequest;
import com.floodrescue.request.domain.enums.RequestStatus;
import com.floodrescue.request.domain.enums.UrgencyLevel;

@Repository
public interface RescueRequestRepository extends JpaRepository<RescueRequest, Long> {

    Page<RescueRequest> findByCitizenId(Long citizenId, Pageable pageable);

    @Query("""
            SELECT r FROM RescueRequest r
            WHERE (:status IS NULL OR r.status = :status)
              AND (:urgencyLevel IS NULL OR r.urgencyLevel = :urgencyLevel)
              AND (:fromDate IS NULL OR r.createdAt >= :fromDate)
              AND (:toDate IS NULL OR r.createdAt <= :toDate)
            ORDER BY r.createdAt DESC
            """)
    Page<RescueRequest> findAllWithFilters(
            @Param("status") RequestStatus status,
            @Param("urgencyLevel") UrgencyLevel urgencyLevel,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

    boolean existsByCitizenIdAndStatusIn(Long citizenId, List<RequestStatus> statuses);

    @Query("""
            SELECT DISTINCT r FROM RescueRequest r
            LEFT JOIN FETCH r.images
            LEFT JOIN FETCH r.statusHistories
            WHERE r.id = :id
            """)
    Optional<RescueRequest> findByIdWithDetails(@Param("id") Long id);
}