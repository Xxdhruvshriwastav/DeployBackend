package com.hireconnect.interview.service;

import com.hireconnect.interview.dto.InterviewDTO;
import com.hireconnect.interview.entity.Interview;
import com.hireconnect.interview.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository itvRepo;

    @Override
    public InterviewDTO scheduleInterview(InterviewDTO dto) {
        Interview interview = convertToEntity(dto);
        interview.setStatus("SCHEDULED");
        return convertToDTO(itvRepo.save(interview));
    }

    @Override
    public String confirmInterview(Long interviewId) {
        Optional<Interview> optionalInterview = itvRepo.findByInterviewId(interviewId);
        if (optionalInterview.isPresent()) {
            Interview interview = optionalInterview.get();
            interview.setStatus("CONFIRMED");
            itvRepo.save(interview);
            return "Interview Confirmed";
        }
        throw new RuntimeException("Interview not found with id: " + interviewId);
    }

    @Override
    public InterviewDTO rescheduleInterview(Long interviewId, LocalDateTime newScheduledAt) {
        Optional<Interview> optionalInterview = itvRepo.findByInterviewId(interviewId);
        if (optionalInterview.isPresent()) {
            Interview interview = optionalInterview.get();
            interview.setScheduledAt(newScheduledAt);
            interview.setStatus("RESCHEDULED");
            return convertToDTO(itvRepo.save(interview));
        }
        throw new RuntimeException("Interview not found with id: " + interviewId);
    }

    @Override
    public void cancelInterview(Long interviewId) {
        Optional<Interview> optionalInterview = itvRepo.findByInterviewId(interviewId);
        if (optionalInterview.isPresent()) {
            Interview interview = optionalInterview.get();
            interview.setStatus("CANCELLED");
            itvRepo.save(interview);
        } else {
            throw new RuntimeException("Interview not found with id: " + interviewId);
        }
    }

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
                .build();
    }
}
