package com.hireconnect.interview.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interviewId;

    private Long applicationId;

    private LocalDateTime scheduledAt;

    private String mode; // "Online", "In-Person"

    private String meetLink;

    private String location;

    private String status; // "SCHEDULED", "CONFIRMED", "RESCHEDULED", "CANCELLED"

    private String notes;

    /** Candidate email — stored so we can send notifications without calling another service */
    private String candidateEmail;

    /** Recruiter email — stored so we can notify recruiter on confirmation/cancellation */
    private String recruiterEmail;

    @PrePersist
    protected void onCreate() {
        if (status == null) status = "SCHEDULED";
    }
}
