package com.hireconnect.job.repository;

import com.hireconnect.job.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByPostedBy(String postedBy);
    List<Job> findByPostedByIgnoreCase(String postedBy);
    List<Job> findByStatus(String status);
    List<Job> findByCategory(String category);
    List<Job> findByLocationContainingIgnoreCase(String location);
    List<Job> findByTitleContainingIgnoreCase(String title);
    long countByPostedBy(String postedBy);
    long countByPostedByIgnoreCase(String postedBy);
}
