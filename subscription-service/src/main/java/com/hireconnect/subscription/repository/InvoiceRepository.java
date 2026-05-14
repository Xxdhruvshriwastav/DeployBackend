package com.hireconnect.subscription.repository;

import com.hireconnect.subscription.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findBySubscriptionId(Long subscriptionId);
    List<Invoice> findByRecruiterId(Long userId);
    Invoice findTopByRecruiterIdOrderByPaymentDateDesc(Long userId);
}
