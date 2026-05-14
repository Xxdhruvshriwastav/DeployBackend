package com.hireconnect.interview.resource;

import com.hireconnect.interview.dto.InterviewDTO;
import com.hireconnect.interview.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
public class InterviewResource {

    private final InterviewService itvService;

    @PostMapping("/schedule")
    public ResponseEntity<InterviewDTO> schedule(@RequestBody InterviewDTO interview) {
        InterviewDTO scheduled = itvService.scheduleInterview(interview);
        return new ResponseEntity<>(scheduled, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<String> confirm(@PathVariable("id") Long interviewId) {
        String response = itvService.confirmInterview(interviewId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<InterviewDTO> reschedule(@PathVariable("id") Long interviewId, @RequestParam String newScheduledAt) {
        // Parsing the date string
        LocalDateTime newDate = LocalDateTime.parse(newScheduledAt);
        InterviewDTO rescheduled = itvService.rescheduleInterview(interviewId, newDate);
        return ResponseEntity.ok(rescheduled);
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable("id") Long interviewId) {
        itvService.cancelInterview(interviewId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/application/{appId}")
    public ResponseEntity<List<InterviewDTO>> getByApplication(@PathVariable("appId") Long applicationId) {
        List<InterviewDTO> interviews = itvService.getByApplication(applicationId);
        return ResponseEntity.ok(interviews);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<InterviewDTO>> getByStatus(@PathVariable("status") String status) {
        List<InterviewDTO> interviews = itvService.getByStatus(status);
        return ResponseEntity.ok(interviews);
    }
}
