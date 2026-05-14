package com.hireconnect.interview.service;

import com.hireconnect.interview.dto.InterviewDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InterviewService {
    
    InterviewDTO scheduleInterview(InterviewDTO interviewDTO);
    
    String confirmInterview(Long interviewId);
    
    InterviewDTO rescheduleInterview(Long interviewId, LocalDateTime newScheduledAt);
    
    void cancelInterview(Long interviewId);
    
    List<InterviewDTO> getByApplication(Long applicationId);
    
    List<InterviewDTO> getByStatus(String status);
    
    Optional<InterviewDTO> getById(Long interviewId);
}
