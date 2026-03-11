package com.floodrescue.dispatch.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.floodrescue.dispatch.domain.entity.Assignment;
import com.floodrescue.dispatch.domain.enums.AssignmentStatus;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    Page<Assignment> findAllByOrderByAssignedAtDesc(Pageable pageable);

    List<Assignment> findByTeamIdOrderByAssignedAtDesc(Long teamId);

    List<Assignment> findByTeamIdAndStatusOrderByAssignedAtDesc(Long teamId, AssignmentStatus status);

    boolean existsByRequestIdAndStatus(Long requestId, AssignmentStatus status);

    Optional<Assignment> findByRequestIdAndStatus(Long requestId, AssignmentStatus status);

    List<Assignment> findByStatus(AssignmentStatus status);
}
