package com.hireconnect.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobDTO {
    private Long jobId;
    private String title;
    private String description;
    private String category;
    private String type;
    private String location;
    private Double salaryMin;
    private Double salaryMax;
    private List<String> skills;
    private String experienceRequired;
    private String postedBy;
    private String status;
    private LocalDateTime postedAt;
}
