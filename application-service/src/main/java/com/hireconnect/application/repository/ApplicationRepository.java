package com.hireconnect.application.repository;

import com.hireconnect.application.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByCandidateEmail(String email);
    List<Application> findByJobId(Long jobId);
    List<Application> findByStatus(String status);
    Optional<Application> findByJobIdAndCandidateEmail(Long jobId, String candidateEmail);
    long countByJobId(Long jobId);
    long countByStatus(String status);
    long countByJobIdIn(List<Long> jobIds);
    long countByJobIdInAndStatus(List<Long> jobIds, String status);
}
