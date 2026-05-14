package com.hireconnect.subscription.repository;

import com.hireconnect.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByRecruiterId(Long userId);
    List<Subscription> findByStatus(String status);
    Optional<Subscription> findByRecruiterIdAndStatus(Long userId, String status);
}
