package com.hireconnect.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserProfileDTO {
    private Long profileId;
    private String email;
    private String fullName;
    private String role;
    private String profilePictureUrl;
    private String coverPictureUrl;
    private String summary;
}
