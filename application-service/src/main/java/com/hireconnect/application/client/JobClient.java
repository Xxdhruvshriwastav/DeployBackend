package com.hireconnect.application.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobClient {

    private final RestTemplate restTemplate;

    private static final String JOB_SERVICE_URL = "http://JOB-SERVICE/api/v1/jobs/";

    /**
     * Fetch the recruiter (postedBy) email for a given jobId.
     */
    @SuppressWarnings("unchecked")
    public String getRecruiterEmail(Long jobId) { // we fetch the email of recuiter,  to send email notification
        try {
            Map<String, Object> job = restTemplate.getForObject(JOB_SERVICE_URL + jobId, Map.class);  // we need specific job details insted of all
                                                                                                            // we mention .class because to conver json responce to object
                                                                                                            // i am not using dto because it reponce fast
            if (job != null && job.containsKey("postedBy")) {
                return (String) job.get("postedBy");   // we can fetch value means email by using map
            }
        } catch (Exception e) {
            log.error("Could not fetch job {} from JOB-SERVICE: {}", jobId, e.getMessage());
        }
        return null;
    }
}


