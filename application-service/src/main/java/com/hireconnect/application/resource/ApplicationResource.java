package com.hireconnect.application.resource;

import com.hireconnect.application.dto.ApplicationDTO;
import com.hireconnect.application.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class ApplicationResource {

    private final ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<ApplicationDTO> submit(@RequestBody ApplicationDTO application) {
        return ResponseEntity.ok(applicationService.submitApplication(application));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countAll() {
        return ResponseEntity.ok(applicationService.countAll());
    }

    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> countByStatus(@PathVariable String status) {
        return ResponseEntity.ok(applicationService.countByStatus(status));
    }

    @PostMapping("/count/jobs")
    public ResponseEntity<Long> countByJobs(@RequestBody List<Long> jobIds) {
        return ResponseEntity.ok(applicationService.countByJobs(jobIds));
    }

    @PostMapping("/count/jobs/status/{status}")
    public ResponseEntity<Long> countByJobsAndStatus(@RequestBody List<Long> jobIds, @PathVariable String status) {
        return ResponseEntity.ok(applicationService.countByJobsAndStatus(jobIds, status));
    }

    @GetMapping("/candidate/{email}")
    public ResponseEntity<List<ApplicationDTO>> getByCandidate(@PathVariable String email) {
        return ResponseEntity.ok(applicationService.getByCandidate(email));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<ApplicationDTO>> getByJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(applicationService.getByJob(jobId));
    }

    @GetMapping("/count/job/{jobId}")
    public ResponseEntity<Long> countByJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(applicationService.countByJob(jobId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationDTO> getById(@PathVariable Long id) {
        return applicationService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApplicationDTO> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(applicationService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> withdraw(@PathVariable Long id) {
        applicationService.withdrawApplication(id);
        return ResponseEntity.noContent().build();
    }
}
