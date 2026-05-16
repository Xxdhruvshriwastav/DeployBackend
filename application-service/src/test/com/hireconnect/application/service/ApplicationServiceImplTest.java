package com.hireconnect.application.service;

import com.hireconnect.application.messaging.JobRpcClient;
import com.hireconnect.application.messaging.NotificationPublisher;
import com.hireconnect.application.dto.ApplicationDTO;
import com.hireconnect.application.entity.Application;
import com.hireconnect.application.repository.ApplicationRepository;
import com.hireconnect.application.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {


    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private NotificationPublisher notificationPublisher;

    @Mock
    private JobRpcClient jobRpcClient;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    private ApplicationDTO applicationDTO;
    private Application application;


    @BeforeEach
    void setUp() {
        applicationDTO = ApplicationDTO.builder()
                .jobId(1L)
                .candidateEmail("test@gmail.com")
                .coverLetter("Test Cover Letter")
                .resumeUrl("resume.pdf")
                .build();

        application = Application.builder()
                .applicationId(1L)
                .jobId(1L)
                .candidateEmail("test@gmail.com")
                .status("APPLIED")
                .coverLetter("Test Cover Letter")
                .resumeUrl("resume.pdf")
                .build();
    }

    // submit application test
    @Test
    void testSubmitApplication_success() {

        when(applicationRepository.findByJobIdAndCandidateEmail(1L, "test@gmail.com")) // checking application exist or not
                .thenReturn(Optional.empty());

        when(applicationRepository.save(any(Application.class)))
                .thenReturn(application);


        when(jobRpcClient.getRecruiterEmail(1L))
                .thenReturn("recruiter@gmail.com");


        ApplicationDTO result = applicationService.submitApplication(applicationDTO);


        assertNotNull(result);
        assertEquals("test@gmail.com", result.getCandidateEmail());

        verify(applicationRepository, times(1)).save(any(Application.class));
        verify(notificationPublisher, times(2)).sendEmail(anyString(), anyString()); // because in service, // 1. Candidate, 2. Recruiter

    }


    // test already applied

    @Test
    void testSubmitApplicationAlreadyApplied() {

        when(applicationRepository.findByJobIdAndCandidateEmail(1L, "test@gmail.com"))
                .thenReturn(Optional.of(application));


        CustomException exception = assertThrows(CustomException.class, () ->
                applicationService.submitApplication(applicationDTO)
        );


        assertTrue(exception.getMessage().contains("already applied"));


    }


  // test to apply again after withdraw

    @Test
    void testSubmitApplication_reapplyAfterWithdrawn() {
        application.setStatus("WITHDRAWN");

        when(applicationRepository.findByJobIdAndCandidateEmail(1L, "test@gmail.com"))
                .thenReturn(Optional.of(application));

        when(applicationRepository.save(any(Application.class)))
                .thenReturn(application);

        when(jobRpcClient.getRecruiterEmail(1L))
                .thenReturn("recruiter@gmail.com");


        ApplicationDTO result = applicationService.submitApplication(applicationDTO);

        assertNotNull(result);

        verify(applicationRepository).delete(application);
        verify(notificationPublisher, times(2)).sendEmail(anyString(), anyString());


    }


// get by id


    @Test
    void testGetByCandidate() {

        when(applicationRepository.findByCandidateEmail("test@gmail.com"))
                .thenReturn(List.of(application));

        List<ApplicationDTO> result = applicationService.getByCandidate("test@gmail.com");

        assertEquals(1, result.size());
        assertEquals("test@gmail.com", result.get(0).getCandidateEmail());
    }

    // test by job

@Test
void testGetByJob() {

    when(applicationRepository.findByJobId(1L))
            .thenReturn(List.of(application));

    List<ApplicationDTO> result = applicationService.getByJob(1L);

    assertEquals(1, result.size());
}



@Test
void testUpdateStatus() {

    when(applicationRepository.findById(1L))
            .thenReturn(Optional.of(application));

    when(applicationRepository.save(any(Application.class)))
            .thenReturn(application);

    ApplicationDTO result = applicationService.updateStatus(1L, "SHORTLISTED");

    assertEquals("SHORTLISTED", result.getStatus());
}


    @Test
    void testUpdateStatus_notFound() {

        when(applicationRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                applicationService.updateStatus(1L, "SHORTLISTED")
        );
    }

    @Test
    void testWithdrawApplication() {

        when(applicationRepository.findById(1L))
                .thenReturn(Optional.of(application));

        applicationService.withdrawApplication(1L);

        assertEquals("WITHDRAWN", application.getStatus());
        verify(applicationRepository).save(application);
    }

    // count methods
    @Test
    void testCountMethods() {

        when(applicationRepository.count()).thenReturn(10L);
        when(applicationRepository.countByStatus("APPLIED")).thenReturn(5L);
        when(applicationRepository.countByJobId(1L)).thenReturn(3L);

        assertEquals(10, applicationService.countAll());
        assertEquals(5, applicationService.countByStatus("APPLIED"));
        assertEquals(3, applicationService.countByJob(1L));
    }





}