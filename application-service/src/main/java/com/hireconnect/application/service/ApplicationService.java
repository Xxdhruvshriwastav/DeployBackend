package com.hireconnect.application.service;

import com.hireconnect.application.dto.ApplicationDTO;
import java.util.List;
import java.util.Optional;

public interface ApplicationService {
    ApplicationDTO submitApplication(ApplicationDTO applicationDTO);
    List<ApplicationDTO> getByCandidate(String email);
    List<ApplicationDTO> getByJob(Long jobId);
    ApplicationDTO updateStatus(Long applicationId, String status);
    void withdrawApplication(Long applicationId);
    Optional<ApplicationDTO> getById(Long applicationId);
    long countByJob(Long jobId);
    long countAll();
    long countByStatus(String status);
    long countByJobs(List<Long> jobIds);
    long countByJobsAndStatus(List<Long> jobIds, String status);
}
