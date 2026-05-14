package com.hireconnect.application.service;

import com.hireconnect.application.client.JobClient;
import com.hireconnect.application.client.NotificationClient;
import com.hireconnect.application.dto.ApplicationDTO;
import com.hireconnect.application.entity.Application;
import com.hireconnect.application.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final NotificationClient notificationClient;
    private final JobClient jobClient;

    @Override
    public ApplicationDTO submitApplication(ApplicationDTO applicationDTO) {
        // Check if already applied
        Optional<Application> existing = applicationRepository.findByJobIdAndCandidateEmail(
                applicationDTO.getJobId(), applicationDTO.getCandidateEmail());
        
        if (existing.isPresent()) {
            Application existingApp = existing.get();
            if (!"WITHDRAWN".equals(existingApp.getStatus())) {
                throw new RuntimeException("You have already applied for this job and your application is " + existingApp.getStatus());
            }
            // If withdrawn, we allow re-applying by deleting the old one or just continuing (we'll save a new one with a new ID or update)
            // Let's delete the withdrawn one to allow a fresh start
            applicationRepository.delete(existingApp);
        }

        Application application = convertToEntity(applicationDTO);
        Application saved = applicationRepository.save(application);

        // ── Send notifications

        String candidateEmail = saved.getCandidateEmail();
        Long jobId = saved.getJobId();

        // 1. Notify candidate
        notificationClient.sendEmail(
            candidateEmail,
            "Your application for Job #" + jobId + " has been submitted successfully! " +
            "We will notify you of any updates. Good luck!"
        );

        // 2. Notify recruiter — fetch recruiter email from Job-Service
        String recruiterEmail = jobClient.getRecruiterEmail(jobId);
        if (recruiterEmail != null && !recruiterEmail.isBlank()) {
            notificationClient.sendEmail(
                recruiterEmail,
                "New application received for Job #" + jobId + "! " +
                "Candidate: " + candidateEmail + " has applied. Review the application in your dashboard."
            );
        } else {
            log.warn("Could not determine recruiter email for jobId={}, skipping recruiter notification.", jobId);
        }

        return convertToDTO(saved);
    }

    @Override
    public List<ApplicationDTO> getByCandidate(String email) {
        return applicationRepository.findByCandidateEmail(email).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationDTO> getByJob(Long jobId) {
        return applicationRepository.findByJobId(jobId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ApplicationDTO updateStatus(Long applicationId, String status) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        application.setStatus(status);
        Application updated = applicationRepository.save(application);
        return convertToDTO(updated);
    }

    @Override
    public void withdrawApplication(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        application.setStatus("WITHDRAWN");
        applicationRepository.save(application);
    }

    @Override
    public Optional<ApplicationDTO> getById(Long applicationId) {
        return applicationRepository.findById(applicationId).map(this::convertToDTO);
    }

    @Override
    public long countByJob(Long jobId) {
        return applicationRepository.countByJobId(jobId);
    }

    @Override
    public long countAll() {
        return applicationRepository.count();
    }

    @Override
    public long countByStatus(String status) {
        return applicationRepository.countByStatus(status);
    }

    @Override
    public long countByJobs(List<Long> jobIds) {
        return applicationRepository.countByJobIdIn(jobIds);
    }

    @Override
    public long countByJobsAndStatus(List<Long> jobIds, String status) {
        return applicationRepository.countByJobIdInAndStatus(jobIds, status);
    }

    private ApplicationDTO convertToDTO(Application application) {
        return ApplicationDTO.builder()
                .applicationId(application.getApplicationId())
                .jobId(application.getJobId())
                .candidateEmail(application.getCandidateEmail())
                .appliedAt(application.getAppliedAt())
                .status(application.getStatus())
                .coverLetter(application.getCoverLetter())
                .resumeUrl(application.getResumeUrl())
                .build();
    }

    private Application convertToEntity(ApplicationDTO dto) {
        return Application.builder()
                .applicationId(dto.getApplicationId())
                .jobId(dto.getJobId())
                .candidateEmail(dto.getCandidateEmail())
                .appliedAt(dto.getAppliedAt())
                .status(dto.getStatus())
                .coverLetter(dto.getCoverLetter())
                .resumeUrl(dto.getResumeUrl())
                .build();
    }
}
