package com.hireconnect.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationDTO {
    private Long applicationId;
    private Long jobId;
    private String candidateEmail;
    private LocalDateTime appliedAt;
    private String status;
    private String coverLetter;
    private String resumeUrl;
}
