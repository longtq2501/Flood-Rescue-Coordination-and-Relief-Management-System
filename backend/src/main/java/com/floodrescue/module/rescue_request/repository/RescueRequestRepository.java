package com.floodrescue.module.rescue_request.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.floodrescue.module.rescue_request.domain.entity.RescueRequest;
import com.floodrescue.module.rescue_request.domain.enums.RequestStatus;
import com.floodrescue.module.rescue_request.domain.enums.UrgencyLevel;

@Repository
public interface RescueRequestRepository extends JpaRepository<RescueRequest, Long> {

  // Citizen xem yêu cầu của mình
  Page<RescueRequest> findByCitizenId(Long citizenId, Pageable pageable);

  // Coordinator xem tất cả — có filter
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

  // Kiểm tra citizen đã có request PENDING hoặc IN_PROGRESS chưa
  boolean existsByCitizenIdAndStatusIn(Long citizenId, List<RequestStatus> statuses);

  // Lấy request kèm images + status history (tránh N+1)
  @Query("""
      SELECT r FROM RescueRequest r
      WHERE r.id = :id
      """)
  java.util.Optional<RescueRequest> findByIdWithDetails(Long id);
}