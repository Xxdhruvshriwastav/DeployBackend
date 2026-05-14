package com.hireconnect.job.controller;

import com.hireconnect.job.dto.JobDTO;
import com.hireconnect.job.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobResource {

    @Autowired
    private JobService jobService;

    @PostMapping
    public ResponseEntity<JobDTO> addJob(@RequestBody JobDTO job) {
        return ResponseEntity.ok(jobService.addJob(job));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countJobs() {
        return ResponseEntity.ok(jobService.countJobs());
    }

    // :.+ regex ensures the full email including .com is captured
    @GetMapping("/count/recruiter/{email:.+}")
    public ResponseEntity<Long> countJobsByRecruiter(@PathVariable String email) {
        return ResponseEntity.ok(jobService.countJobsByRecruiter(email));
    }

    @GetMapping
    public ResponseEntity<List<JobDTO>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobDTO> getJobById(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    // :.+ regex ensures the full email including .com is captured
    @GetMapping("/recruiter/{email:.+}")
    public ResponseEntity<List<JobDTO>> getJobsByRecruiter(@PathVariable String email) {
        System.out.println("DEBUG: Fetching jobs for recruiter email: " + email);
        return ResponseEntity.ok(jobService.getJobsByRecruiter(email));
    }

    @GetMapping("/search")
    public ResponseEntity<List<JobDTO>> searchJobs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location) {
        return ResponseEntity.ok(jobService.searchJobs(title, category, location));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobDTO> updateJob(@PathVariable Long id, @RequestBody JobDTO job) {
        return ResponseEntity.ok(jobService.updateJob(id, job));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }
}
