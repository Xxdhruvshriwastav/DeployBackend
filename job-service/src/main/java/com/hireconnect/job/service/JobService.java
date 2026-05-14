package com.hireconnect.job.service;

import com.hireconnect.job.dto.JobDTO;
import java.util.List;

public interface JobService {
    JobDTO addJob(JobDTO jobDTO);
    List<JobDTO> getAllJobs();
    JobDTO getJobById(Long jobId);
    List<JobDTO> getJobsByRecruiter(String recruiterEmail);
    List<JobDTO> searchJobs(String title, String category, String location);
    JobDTO updateJob(Long jobId, JobDTO jobDTO);
    void deleteJob(Long jobId);
    long countJobs();
    long countJobsByRecruiter(String recruiterEmail);
}
