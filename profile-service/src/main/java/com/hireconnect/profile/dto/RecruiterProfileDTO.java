package com.hireconnect.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RecruiterProfileDTO extends UserProfileDTO {
    private String companyName;
    private String companySize;
    private String industry;
    private String website;
}
