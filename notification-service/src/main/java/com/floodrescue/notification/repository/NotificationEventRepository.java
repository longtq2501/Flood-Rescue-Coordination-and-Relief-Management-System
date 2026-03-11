package com.floodrescue.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.floodrescue.notification.domain.entity.NotificationEvent;
import com.floodrescue.notification.domain.enums.NotificationStatus;

@Repository
public interface NotificationEventRepository extends JpaRepository<NotificationEvent, Long> {

    Page<NotificationEvent> findByTargetUserIdOrderByCreatedAtDesc(
            Long targetUserId, Pageable pageable);

    long countByTargetUserIdAndStatus(Long targetUserId, NotificationStatus status);
}
