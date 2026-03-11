package com.floodrescue.request.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.floodrescue.request.domain.entity.StatusHistory;

@Repository
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {

    List<StatusHistory> findByRequestIdOrderByChangedAtAsc(Long requestId);
}
