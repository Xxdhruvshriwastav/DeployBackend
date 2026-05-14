package com.hireconnect.job.service;

import com.hireconnect.job.dto.JobDTO;
import com.hireconnect.job.entity.Job;
import com.hireconnect.job.repository.JobRepository;
import com.hireconnect.job.dto.NotificationMessage;
import com.hireconnect.job.publisher.RabbitMQProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class JobServiceImpl implements JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private RabbitMQProducer rabbitMQProducer;

    @Override
    @Transactional
    public JobDTO addJob(JobDTO jobDTO) {
        Job job = convertToEntity(jobDTO);
        Job savedJob = jobRepository.save(job);

        // Send notification via RabbitMQ (wrapped so a RabbitMQ failure doesn't fail the job post)
        try {
            NotificationMessage message = NotificationMessage.builder()
                    .userId(savedJob.getPostedBy())
                    .type("JOB_POSTED")
                    .message("Successfully posted new job: " + savedJob.getTitle())
                    .build();
            rabbitMQProducer.sendNotification(message);
        } catch (Exception e) {
            System.err.println("Warning: RabbitMQ notification failed for job " + savedJob.getJobId() + ": " + e.getMessage());
        }

        return convertToDTO(savedJob);
    }

    @Override
    public List<JobDTO> getAllJobs() {
        return jobRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public JobDTO getJobById(Long jobId) {
        return jobRepository.findById(jobId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobDTO> getJobsByRecruiter(String recruiterEmail) {
     //   System.out.println("DEBUG: getJobsByRecruiter called for: " + recruiterEmail);
        List<Job> jobs = jobRepository.findByPostedByIgnoreCase(recruiterEmail);
        System.out.println("DEBUG: Found " + jobs.size() + " jobs");
        return jobs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobDTO> searchJobs(String title, String category, String location) {
        List<Job> jobs = jobRepository.findAll();

        if (title != null && !title.isEmpty()) {
            jobs = jobs.stream().filter(j -> j.getTitle().toLowerCase().contains(title.toLowerCase())).collect(Collectors.toList());
        }
        if (category != null && !category.isEmpty()) {
            jobs = jobs.stream().filter(j -> j.getCategory().equalsIgnoreCase(category)).collect(Collectors.toList());
        }
        if (location != null && !location.isEmpty()) {
            jobs = jobs.stream().filter(j -> j.getLocation().toLowerCase().contains(location.toLowerCase())).collect(Collectors.toList());
        }

        return jobs.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public JobDTO updateJob(Long jobId, JobDTO jobDTO) {
        Job existingJob = jobRepository.findById(jobId).orElse(null);
        if (existingJob != null) {
            existingJob.setTitle(jobDTO.getTitle());
            existingJob.setDescription(jobDTO.getDescription());
            existingJob.setCategory(jobDTO.getCategory());
            existingJob.setType(jobDTO.getType());
            existingJob.setLocation(jobDTO.getLocation());
            existingJob.setSalaryMin(jobDTO.getSalaryMin());
            existingJob.setSalaryMax(jobDTO.getSalaryMax());
            existingJob.setSkills(jobDTO.getSkills());
            existingJob.setExperienceRequired(jobDTO.getExperienceRequired());
            existingJob.setStatus(jobDTO.getStatus());
            Job updatedJob = jobRepository.save(existingJob);
            return convertToDTO(updatedJob);
        }
        return null;
    }

    @Override
    @Transactional
    public void deleteJob(Long jobId) {
        jobRepository.deleteById(jobId);
    }

    @Override
    public long countJobs() {
        return jobRepository.count();
    }

    @Override
    public long countJobsByRecruiter(String recruiterEmail) {
        return jobRepository.countByPostedByIgnoreCase(recruiterEmail);
    }

    private JobDTO convertToDTO(Job job) {
        return JobDTO.builder()
                .jobId(job.getJobId())
                .title(job.getTitle())
                .description(job.getDescription())
                .category(job.getCategory())
                .type(job.getType())
                .location(job.getLocation())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .skills(job.getSkills() != null ? new ArrayList<>(job.getSkills()) : new ArrayList<>())
                .experienceRequired(job.getExperienceRequired())
                .postedBy(job.getPostedBy())
                .status(job.getStatus())
                .postedAt(job.getPostedAt())
                .build();
    }

    private Job convertToEntity(JobDTO dto) {
        return Job.builder()
                .jobId(dto.getJobId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .type(dto.getType())
                .location(dto.getLocation())
                .salaryMin(dto.getSalaryMin())
                .salaryMax(dto.getSalaryMax())
                .skills(dto.getSkills())
                .experienceRequired(dto.getExperienceRequired())
                .postedBy(dto.getPostedBy())
                .status(dto.getStatus())
                .build();
    }
}
