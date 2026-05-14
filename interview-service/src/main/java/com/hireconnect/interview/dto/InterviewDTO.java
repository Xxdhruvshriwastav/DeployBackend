package com.hireconnect.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewDTO {
    private Long interviewId;
    private Long applicationId;
    private LocalDateTime scheduledAt;
    private String mode;
    private String meetLink;
    private String location;
    private String status;
    private String notes;
}
