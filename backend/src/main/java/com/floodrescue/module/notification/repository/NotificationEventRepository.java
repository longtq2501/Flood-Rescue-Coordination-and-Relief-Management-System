package com.floodrescue.module.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.floodrescue.module.notification.domain.entity.NotificationEvent;
import com.floodrescue.module.notification.domain.enums.NotificationStatus;

@Repository
public interface NotificationEventRepository extends JpaRepository<NotificationEvent, Long> {

    // User xem lịch sử notification của mình
    Page<NotificationEvent> findByTargetUserIdOrderByCreatedAtDesc(
            Long targetUserId, Pageable pageable);

    // Đếm notification chưa đọc (PENDING)
    long countByTargetUserIdAndStatus(Long targetUserId, NotificationStatus status);
}