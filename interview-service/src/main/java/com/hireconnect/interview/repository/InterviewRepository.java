package com.hireconnect.interview.repository;

import com.hireconnect.interview.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {
    
    List<Interview> findByApplicationId(Long applicationId);


    List<Interview> findByStatus(String status);
    
    List<Interview> findByScheduledAtBetween(LocalDateTime start, LocalDateTime end);
    
    Optional<Interview> findByInterviewId(Long interviewId);
    
    void deleteByInterviewId(Long interviewId);
}
