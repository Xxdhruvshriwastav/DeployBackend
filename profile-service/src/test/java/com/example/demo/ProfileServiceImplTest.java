package com.example.demo;


import com.hireconnect.profile.dto.CandidateProfileDTO;
import com.hireconnect.profile.entity.CandidateProfile;
import com.hireconnect.profile.repository.ProfileRepository;
import com.hireconnect.profile.service.ProfileServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceImplTest {


    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private ProfileServiceImpl profileService;


    // add candidate

    @Test
    void testAddCandidateProfile() {

        CandidateProfileDTO dto = new CandidateProfileDTO();
        dto.setEmail("test@gmail.com");
        dto.setFullName("Ashish");

        CandidateProfile saved = new CandidateProfile();
        saved.setEmail("test@gmail.com");

        when(profileRepository.save(any())).thenReturn(saved);

        CandidateProfileDTO result = profileService.addCandidateProfile(dto);

        assertEquals("test@gmail.com", result.getEmail());
        verify(profileRepository).save(any());

    }
    // get profile success

        @Test
        void testGetProfileByEmail(){

            CandidateProfile profile = new CandidateProfile();
            profile.setEmail("test@gmail.com");

            when(profileRepository.findByEmail("test@gmail.com"))
                    .thenReturn(Optional.of(profile));

            var result = profileService.getProfileByEmail("test@gmail.com");
            assertEquals("test@gmail.com", result.getEmail());

        }
        
    // Get profile fail

    @Test
    void testGetProfileByEmail_NOtFound() {

        when(profileRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.empty());


        assertThrows(RuntimeException.class, () ->
                profileService.getProfileByEmail("test@gmail.com"));
    }


    // Delete Profile
    @Test
    void testDeleteProfile() {

        CandidateProfile profile = new CandidateProfile();

        when(profileRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(profile));

        profileService.deleteProfile("test@gmail.com");

        verify(profileRepository).delete(profile);
    }


    @Test
    void testUpdateProfileNotFound() {

        when(profileRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.empty());

        CandidateProfileDTO dto = new CandidateProfileDTO();

        assertThrows(RuntimeException.class, () ->
                profileService.updateProfile("test@gmail.com", dto));

    }
}
