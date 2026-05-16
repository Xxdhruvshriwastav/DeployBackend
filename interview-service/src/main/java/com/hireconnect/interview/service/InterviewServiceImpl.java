package com.hireconnect.interview.service;

import com.hireconnect.interview.dto.InterviewDTO;
import com.hireconnect.interview.entity.Interview;
import com.hireconnect.interview.messaging.InterviewNotificationPublisher;
import com.hireconnect.interview.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository itvRepo;
    private final InterviewNotificationPublisher notificationPublisher;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public InterviewDTO scheduleInterview(InterviewDTO dto) {
        Interview interview = convertToEntity(dto);
        interview.setStatus("SCHEDULED");
        Interview saved = itvRepo.save(interview);

        // Notify candidate via RabbitMQ
        if (dto.getCandidateEmail() != null) {
            String dateStr = saved.getScheduledAt() != null
                    ? saved.getScheduledAt().format(FMT) : "TBD";
            String modeInfo = buildModeInfo(saved);

            notificationPublisher.notify(
                    dto.getCandidateEmail(),
                    "EMAIL",
                    "Dear Candidate,\n\n"
                    + "Your interview has been SCHEDULED.\n\n"
                    + "Details:\n"
                    + "  Date & Time : " + dateStr + "\n"
                    + "  Mode        : " + saved.getMode() + "\n"
                    + modeInfo
                    + (saved.getNotes() != null && !saved.getNotes().isBlank()
                            ? "  Notes       : " + saved.getNotes() + "\n" : "")
                    + "\nPlease confirm your availability.\n\n"
                    + "Regards,\nHireConnect Team"
            );
        }

        // Notify recruiter if email provided
        if (dto.getRecruiterEmail() != null) {
            notificationPublisher.notify(
                    dto.getRecruiterEmail(),
                    "INFO",
                    "Interview scheduled successfully for application #"
                    + saved.getApplicationId()
                    + ". Candidate has been notified."
            );
        }

        return convertToDTO(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public String confirmInterview(Long interviewId) {
        Interview interview = itvRepo.findByInterviewId(interviewId)
                .orElseThrow(() -> new RuntimeException("Interview not found with id: " + interviewId));

        interview.setStatus("CONFIRMED");
        itvRepo.save(interview);

        // Notify candidate
        if (interview.getCandidateEmail() != null) {
            String dateStr = interview.getScheduledAt() != null
                    ? interview.getScheduledAt().format(FMT) : "TBD";
            notificationPublisher.notify(
                    interview.getCandidateEmail(),
                    "EMAIL",
                    "Dear Candidate,\n\n"
                    + "Your interview has been CONFIRMED.\n\n"
                    + "  Date & Time : " + dateStr + "\n"
                    + "  Mode        : " + interview.getMode() + "\n"
                    + buildModeInfo(interview)
                    + "\nBest of luck!\n\n"
                    + "Regards,\nHireConnect Team"
            );
        }

        return "Interview Confirmed";
    }

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public InterviewDTO rescheduleInterview(Long interviewId, LocalDateTime newScheduledAt) {
        Interview interview = itvRepo.findByInterviewId(interviewId)
                .orElseThrow(() -> new RuntimeException("Interview not found with id: " + interviewId));

        interview.setScheduledAt(newScheduledAt);
        interview.setStatus("RESCHEDULED");
        Interview saved = itvRepo.save(interview);

        // Notify candidate of new date
        if (interview.getCandidateEmail() != null) {
            String newDateStr = newScheduledAt.format(FMT);
            notificationPublisher.notify(
                    interview.getCandidateEmail(),
                    "EMAIL",
                    "Dear Candidate,\n\n"
                    + "Your interview has been RESCHEDULED.\n\n"
                    + "  New Date & Time : " + newDateStr + "\n"
                    + "  Mode            : " + interview.getMode() + "\n"
                    + buildModeInfo(interview)
                    + "\nPlease confirm your availability for the new slot.\n\n"
                    + "Regards,\nHireConnect Team"
            );
        }

        return convertToDTO(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void cancelInterview(Long interviewId) {
        Interview interview = itvRepo.findByInterviewId(interviewId)
                .orElseThrow(() -> new RuntimeException("Interview not found with id: " + interviewId));

        interview.setStatus("CANCELLED");
        itvRepo.save(interview);

        // Notify candidate
        if (interview.getCandidateEmail() != null) {
            notificationPublisher.notify(
                    interview.getCandidateEmail(),
                    "EMAIL",
                    "Dear Candidate,\n\n"
                    + "We regret to inform you that your interview (Application #"
                    + interview.getApplicationId()
                    + ") has been CANCELLED.\n\n"
                    + "Our recruiter will be in touch to reschedule.\n\n"
                    + "Regards,\nHireConnect Team"
            );
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public List<InterviewDTO> getByApplication(Long applicationId) {
        return itvRepo.findByApplicationId(applicationId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InterviewDTO> getByStatus(String status) {
        return itvRepo.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<InterviewDTO> getById(Long interviewId) {
        return itvRepo.findByInterviewId(interviewId).map(this::convertToDTO);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /** Build meet link or location line based on mode */
    private String buildModeInfo(Interview interview) {
        if ("Online".equalsIgnoreCase(interview.getMode()) && interview.getMeetLink() != null) {
            return "  Meet Link   : " + interview.getMeetLink() + "\n";
        } else if (interview.getLocation() != null && !interview.getLocation().isBlank()) {
            return "  Location    : " + interview.getLocation() + "\n";
        }
        return "";
    }

    private InterviewDTO convertToDTO(Interview entity) {
        return InterviewDTO.builder()
                .interviewId(entity.getInterviewId())
                .applicationId(entity.getApplicationId())
                .scheduledAt(entity.getScheduledAt())
                .mode(entity.getMode())
                .meetLink(entity.getMeetLink())
                .location(entity.getLocation())
                .status(entity.getStatus())
                .notes(entity.getNotes())
                .candidateEmail(entity.getCandidateEmail())
                .recruiterEmail(entity.getRecruiterEmail())
                .build();
    }

    private Interview convertToEntity(InterviewDTO dto) {
        return Interview.builder()
                .interviewId(dto.getInterviewId())
                .applicationId(dto.getApplicationId())
                .scheduledAt(dto.getScheduledAt())
                .mode(dto.getMode())
                .meetLink(dto.getMeetLink())
                .location(dto.getLocation())
                .status(dto.getStatus())
                .notes(dto.getNotes())
                .candidateEmail(dto.getCandidateEmail())
                .recruiterEmail(dto.getRecruiterEmail())
                .build();
    }
}
