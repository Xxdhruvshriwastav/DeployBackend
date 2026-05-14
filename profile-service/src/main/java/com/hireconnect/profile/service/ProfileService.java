package com.hireconnect.profile.service;

import com.hireconnect.profile.dto.CandidateProfileDTO;
import com.hireconnect.profile.dto.RecruiterProfileDTO;
import com.hireconnect.profile.dto.UserProfileDTO;

public interface ProfileService {
    CandidateProfileDTO addCandidateProfile(CandidateProfileDTO profileDTO);
    RecruiterProfileDTO addRecruiterProfile(RecruiterProfileDTO profileDTO);
    UserProfileDTO updateProfile(String email, UserProfileDTO profileData);
    UserProfileDTO getProfileByEmail(String email);
    void deleteProfile(String email);
}
