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
    
    private String status; // "SCHEDULED", "COMPLETED", "CANCELLED", "RESCHEDULED"
    
    private String notes;
    
    @PrePersist
    protected void onCreate() {
        if (status == null) status = "SCHEDULED";
    }
}
