package com.hireconnect.profile.service;

import com.hireconnect.profile.dto.AddressDTO;
import com.hireconnect.profile.dto.CandidateProfileDTO;
import com.hireconnect.profile.dto.RecruiterProfileDTO;
import com.hireconnect.profile.dto.UserProfileDTO;
import com.hireconnect.profile.entity.Address;
import com.hireconnect.profile.entity.CandidateProfile;
import com.hireconnect.profile.entity.RecruiterProfile;
import com.hireconnect.profile.entity.UserProfile;
import com.hireconnect.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import com.hireconnect.profile.exception.CustomException;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;

    @Override
    @Transactional
    public CandidateProfileDTO addCandidateProfile(CandidateProfileDTO profileDTO) {
        CandidateProfile profile = convertToCandidateEntity(profileDTO);
        profile.setRole("CANDIDATE");
        return convertToCandidateDTO(profileRepository.save(profile));
    }

    @Override
    @Transactional
    public RecruiterProfileDTO addRecruiterProfile(RecruiterProfileDTO profileDTO) {
        RecruiterProfile profile = convertToRecruiterEntity(profileDTO);
        profile.setRole("RECRUITER");
        return convertToRecruiterDTO(profileRepository.save(profile));
    }

    @Override
    @Transactional
    public UserProfileDTO updateProfile(String email, UserProfileDTO profileData) {
        Optional<UserProfile> existingOpt = profileRepository.findByEmail(email);
        if (existingOpt.isEmpty()) {
            throw new CustomException("Profile not found for email: " + email, HttpStatus.NOT_FOUND);
        }

        UserProfile existing = existingOpt.get();
        existing.setFullName(profileData.getFullName());
        existing.setProfilePictureUrl(profileData.getProfilePictureUrl());
        existing.setCoverPictureUrl(profileData.getCoverPictureUrl());
        existing.setSummary(profileData.getSummary());

        if (existing instanceof CandidateProfile && profileData instanceof CandidateProfileDTO) {
            CandidateProfile existingCandidate = (CandidateProfile) existing;
            CandidateProfileDTO newCandidate = (CandidateProfileDTO) profileData;
            existingCandidate.setMobile(newCandidate.getMobile());
            existingCandidate.setExperience(newCandidate.getExperience());
            existingCandidate.setResumeUrl(newCandidate.getResumeUrl());

            // Null-safe list replacement for @ElementCollection (skills)
            List<String> newSkills = newCandidate.getSkills() != null
                    ? newCandidate.getSkills() : new ArrayList<>();
            existingCandidate.getSkills().clear();
            existingCandidate.getSkills().addAll(newSkills);

            // Null-safe replacement for JSON-converted lists
            existingCandidate.setEducation(
                newCandidate.getEducation() != null ? newCandidate.getEducation() : new ArrayList<>());
            existingCandidate.setWorkExperience(
                newCandidate.getWorkExperience() != null ? newCandidate.getWorkExperience() : new ArrayList<>());

            // Null-safe replacement for @OneToMany addresses
            if (newCandidate.getAddresses() != null) {
                existingCandidate.getAddresses().clear();
                existingCandidate.getAddresses().addAll(
                    newCandidate.getAddresses().stream()
                        .map(this::convertToAddressEntity)
                        .collect(Collectors.toList())
                );
            }
            return convertToCandidateDTO(profileRepository.save(existingCandidate));
        } else if (existing instanceof RecruiterProfile && profileData instanceof RecruiterProfileDTO) {
            RecruiterProfile existingRecruiter = (RecruiterProfile) existing;
            RecruiterProfileDTO newRecruiter = (RecruiterProfileDTO) profileData;
            existingRecruiter.setCompanyName(newRecruiter.getCompanyName());
            existingRecruiter.setCompanySize(newRecruiter.getCompanySize());
            existingRecruiter.setIndustry(newRecruiter.getIndustry());
            existingRecruiter.setWebsite(newRecruiter.getWebsite());
            return convertToRecruiterDTO(profileRepository.save(existingRecruiter));
        }

        return convertToUserProfileDTO(profileRepository.save(existing));
    }

    @Override
    public UserProfileDTO getProfileByEmail(String email) {
        UserProfile profile = profileRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Profile not found for email: " + email, HttpStatus.NOT_FOUND));
        
        if (profile instanceof CandidateProfile) {
            return convertToCandidateDTO((CandidateProfile) profile);
        } else if (profile instanceof RecruiterProfile) {
            return convertToRecruiterDTO((RecruiterProfile) profile);
        }
        return convertToUserProfileDTO(profile);
    }

    @Override
    @Transactional
    public void deleteProfile(String email) {
        profileRepository.findByEmail(email).ifPresent(profileRepository::delete);
    }

    // Helper Methods
    private CandidateProfile convertToCandidateEntity(CandidateProfileDTO dto) {
        return CandidateProfile.builder()
                .profileId(dto.getProfileId())
                .email(dto.getEmail())
                .fullName(dto.getFullName())
                .role(dto.getRole())
                .profilePictureUrl(dto.getProfilePictureUrl())
                .coverPictureUrl(dto.getCoverPictureUrl())
                .summary(dto.getSummary())
                .mobile(dto.getMobile())
                .experience(dto.getExperience())
                .resumeUrl(dto.getResumeUrl())
                .skills(dto.getSkills())
                .education(dto.getEducation())
                .workExperience(dto.getWorkExperience())
                .addresses(dto.getAddresses() != null ? 
                    dto.getAddresses().stream().map(this::convertToAddressEntity).collect(Collectors.toList()) : null)
                .build();
    }

    private CandidateProfileDTO convertToCandidateDTO(CandidateProfile entity) {
        return CandidateProfileDTO.builder()
                .profileId(entity.getProfileId())
                .email(entity.getEmail())
                .fullName(entity.getFullName())
                .role(entity.getRole())
                .profilePictureUrl(entity.getProfilePictureUrl())
                .coverPictureUrl(entity.getCoverPictureUrl())
                .summary(entity.getSummary())
                .mobile(entity.getMobile())
                .experience(entity.getExperience())
                .resumeUrl(entity.getResumeUrl())
                .skills(entity.getSkills())
                .education(entity.getEducation())
                .workExperience(entity.getWorkExperience())
                .addresses(entity.getAddresses() != null ? 
                    entity.getAddresses().stream().map(this::convertToAddressDTO).collect(Collectors.toList()) : null)
                .build();
    }

    private RecruiterProfile convertToRecruiterEntity(RecruiterProfileDTO dto) {
        return RecruiterProfile.builder()
                .profileId(dto.getProfileId())
                .email(dto.getEmail())
                .fullName(dto.getFullName())
                .role(dto.getRole())
                .profilePictureUrl(dto.getProfilePictureUrl())
                .coverPictureUrl(dto.getCoverPictureUrl())
                .summary(dto.getSummary())
                .companyName(dto.getCompanyName())
                .companySize(dto.getCompanySize())
                .industry(dto.getIndustry())
                .website(dto.getWebsite())
                .build();
    }

    private RecruiterProfileDTO convertToRecruiterDTO(RecruiterProfile entity) {
        return RecruiterProfileDTO.builder()
                .profileId(entity.getProfileId())
                .email(entity.getEmail())
                .fullName(entity.getFullName())
                .role(entity.getRole())
                .profilePictureUrl(entity.getProfilePictureUrl())
                .coverPictureUrl(entity.getCoverPictureUrl())
                .summary(entity.getSummary())
                .companyName(entity.getCompanyName())
                .companySize(entity.getCompanySize())
                .industry(entity.getIndustry())
                .website(entity.getWebsite())
                .build();
    }

    private UserProfileDTO convertToUserProfileDTO(UserProfile entity) {
        return UserProfileDTO.builder()
                .profileId(entity.getProfileId())
                .email(entity.getEmail())
                .fullName(entity.getFullName())
                .role(entity.getRole())
                .profilePictureUrl(entity.getProfilePictureUrl())
                .coverPictureUrl(entity.getCoverPictureUrl())
                .summary(entity.getSummary())
                .build();
    }

    private Address convertToAddressEntity(AddressDTO dto) {
        return Address.builder()
                .addressId(dto.getAddressId())
                .houseNo(dto.getHouseNo())
                .street(dto.getStreet())
                .city(dto.getCity())
                .state(dto.getState())
                .pincode(dto.getPincode())
                .build();
    }

    private AddressDTO convertToAddressDTO(Address entity) {
        return AddressDTO.builder()
                .addressId(entity.getAddressId())
                .houseNo(entity.getHouseNo())
                .street(entity.getStreet())
                .city(entity.getCity())
                .state(entity.getState())
                .pincode(entity.getPincode())
                .build();
    }
}
