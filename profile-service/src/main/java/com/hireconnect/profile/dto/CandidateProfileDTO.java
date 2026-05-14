package com.hireconnect.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CandidateProfileDTO extends UserProfileDTO {
    private String mobile;
    private String experience;
    private String resumeUrl;
    private List<String> skills = new ArrayList<>();
    private List<AddressDTO> addresses = new ArrayList<>();
    private List<Map<String, Object>> education = new ArrayList<>();
    private List<Map<String, Object>> workExperience = new ArrayList<>();
}
