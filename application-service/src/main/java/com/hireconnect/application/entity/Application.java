package com.hireconnect.application.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicationId;

    private Long jobId;
    private String candidateEmail; // Linking by email for simplicity in this case study
    
    private LocalDateTime appliedAt;
    private String status; // APPLIED, SHORTLISTED, INTERVIEW_SCHEDULED, OFFERED, REJECTED, WITHDRAWN
    
    @Column(length = 2000)  // here i got issue
    private String coverLetter;
    
    private String resumeUrl;

    @PrePersist
    protected void onCreate() {
        appliedAt = LocalDateTime.now();
        if (status == null) status = "APPLIED";
    }
}
