package com.hireconnect.application.service;

import com.hireconnect.application.messaging.JobRpcClient;
import com.hireconnect.application.messaging.NotificationPublisher;
import com.hireconnect.application.dto.ApplicationDTO;
import com.hireconnect.application.entity.Application;
import com.hireconnect.application.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import com.hireconnect.application.exception.CustomException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final NotificationPublisher notificationPublisher;
    private final JobRpcClient jobRpcClient;

    @Override
    public ApplicationDTO submitApplication(ApplicationDTO applicationDTO) {
        // Check if already applied
        Optional<Application> existing = applicationRepository.findByJobIdAndCandidateEmail(
                applicationDTO.getJobId(), applicationDTO.getCandidateEmail());

        if (existing.isPresent()) {
            Application existingApp = existing.get();
            if (!"WITHDRAWN".equals(existingApp.getStatus())) {
                throw new CustomException("You have already applied for this job and your application is "
                        + existingApp.getStatus(), HttpStatus.CONFLICT);
            }
            applicationRepository.delete(existingApp);
        }

        Application application = convertToEntity(applicationDTO);
        Application saved = applicationRepository.save(application);

        String candidateEmail = saved.getCandidateEmail();
        Long jobId = saved.getJobId();

        // 1. Notify candidate via RabbitMQ (fire-and-forget)
        notificationPublisher.sendEmail(
                candidateEmail,
                "Your application for Job #" + jobId + " has been submitted successfully! " +
                "We will notify you of any updates. Good luck!"
        );

        // 2. Fetch recruiter email via RabbitMQ RPC, then notify recruiter
        String recruiterEmail = jobRpcClient.getRecruiterEmail(jobId);
        if (recruiterEmail != null && !recruiterEmail.isBlank()) {
            notificationPublisher.sendEmail(
                    recruiterEmail,
                    "New application received for Job #" + jobId + "! " +
                    "Candidate: " + candidateEmail + " has applied. Review in your dashboard."
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
                .orElseThrow(() -> new CustomException("Application not found", HttpStatus.NOT_FOUND));
        application.setStatus(status);
        Application updated = applicationRepository.save(application);
        return convertToDTO(updated);
    }

    @Override
    public void withdrawApplication(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException("Application not found", HttpStatus.NOT_FOUND));
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
