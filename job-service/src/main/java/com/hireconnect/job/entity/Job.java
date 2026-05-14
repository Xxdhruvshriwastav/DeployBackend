package com.hireconnect.job.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jobId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String category;
    private String type; // Full-time, Part-time, etc.
    private String location;
    private Double salaryMin;
    private Double salaryMax;

    // EAGER ensures skills are loaded with the job in the same query
    // Prevents LazyInitializationException (500 error)
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> skills;

    private String experienceRequired;
    private String postedBy; // Email or Recruiter ID
    private String status; // OPEN, CLOSED, PAUSED

    private LocalDateTime postedAt;

    @PrePersist
    protected void onCreate() {
        postedAt = LocalDateTime.now();
        if (status == null) status = "OPEN";
    }
}
