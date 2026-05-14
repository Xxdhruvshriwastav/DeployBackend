package com.hireconnect.profile.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long profileId;

    @Column(unique = true, nullable = false)
    private String email;

    private String fullName;

    @Column(nullable = false)
    private String role; // "CANDIDATE" or "RECRUITER"

    private String profilePictureUrl;
    
    @Column(columnDefinition = "TEXT")
    private String coverPictureUrl;
    
    @Column(columnDefinition = "TEXT")
    private String summary;
}
