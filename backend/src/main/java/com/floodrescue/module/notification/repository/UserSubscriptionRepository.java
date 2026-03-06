package com.floodrescue.module.notification.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.floodrescue.module.notification.domain.entity.UserSubscription;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

    List<UserSubscription> findByUserIdAndActiveTrue(Long userId);

    Optional<UserSubscription> findByUserIdAndTopic(Long userId, String topic);
}