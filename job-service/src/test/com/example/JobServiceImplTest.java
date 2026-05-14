package com.example;

import com.hireconnect.job.dto.JobDTO;
import com.hireconnect.job.entity.Job;
import com.hireconnect.job.repository.JobRepository;
import com.hireconnect.job.publisher.RabbitMQProducer;
import com.hireconnect.job.service.JobServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private RabbitMQProducer rabbitMQProducer;

    @InjectMocks
    private JobServiceImpl jobService;

 // add jobTest
    @Test
    void testAddJob() {

        JobDTO dto = JobDTO.builder()
                .title("Java Dev")
                .postedBy("test@gmail.com")
                .build();

        Job saved = new Job();
        saved.setJobId(1L);
        saved.setTitle("Java Dev");
        saved.setPostedBy("test@gmail.com");

        when(jobRepository.save(any())).thenReturn(saved);

        JobDTO result = jobService.addJob(dto);

        assertEquals("Java Dev", result.getTitle());

        verify(jobRepository).save(any());
        verify(rabbitMQProducer).sendNotification(any());
    }

    // get all jobs

    @Test
    void testGetAllJobs() {

        List<Job> jobs = List.of(new Job(), new Job());

        when(jobRepository.findAll()).thenReturn(jobs);

        List<JobDTO> result = jobService.getAllJobs();

        assertEquals(2, result.size());
    }

  // get jobs by id
    @Test
    void testGetJobById() {

        Job job = new Job();
        job.setJobId(1L);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        JobDTO result = jobService.getJobById(1L);

        assertNotNull(result);
    }

    // job not found

    @Test
    void testGetJobById_NotFound() {

        when(jobRepository.findById(1L)).thenReturn(Optional.empty());

        JobDTO result = jobService.getJobById(1L);

        assertNull(result);
    }

    // get jobs by recuiter
    @Test
    void testGetJobsByRecruiter() {

        List<Job> jobs = List.of(new Job(), new Job());

        when(jobRepository.findByPostedBy("test@gmail.com")).thenReturn(jobs);

        List<JobDTO> result = jobService.getJobsByRecruiter("test@gmail.com");

        assertEquals(2, result.size());
    }

    // update jobs by recuiter
    @Test
    void testUpdateJob() {

        Job job = new Job();
        job.setJobId(1L);

        JobDTO dto = new JobDTO();
        dto.setTitle("Updated Job");

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenReturn(job);

        JobDTO result = jobService.updateJob(1L, dto);

        assertNotNull(result);
    }

    // update fail
    @Test
    void testUpdateJob_NotFound() {

        JobDTO dto = new JobDTO();

        when(jobRepository.findById(1L)).thenReturn(Optional.empty());

        JobDTO result = jobService.updateJob(1L, dto);

        assertNull(result);
    }

// delete jobs
    @Test
    void testDeleteJob() {

        jobService.deleteJob(1L);

        verify(jobRepository).deleteById(1L);
    }

    // counts jobs
    @Test
    void testCountJobs() {

        when(jobRepository.count()).thenReturn(5L);

        long result = jobService.countJobs();

        assertEquals(5, result);
    }

    // counts by recuiter
    @Test
    void testCountJobsByRecruiter() {

        when(jobRepository.countByPostedBy("test@gmail.com")).thenReturn(3L);

        long result = jobService.countJobsByRecruiter("test@gmail.com");

        assertEquals(3, result);
    }
}

