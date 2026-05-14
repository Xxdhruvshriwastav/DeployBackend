package com.hireconnect.profile.entity;

import com.hireconnect.profile.converter.JsonListConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CandidateProfile extends UserProfile {

    private String mobile;
    
    private String experience;
    
    private String resumeUrl;

    @ElementCollection(fetch = FetchType.EAGER) // fetch data from database eagerly not lazy, as well it creates a diffrent table and and id will be same and have diffrent skills
    @CollectionTable(name = "candidate_skills", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER) // means everything linked, if you add new address will be add, if you delete address will delete as well
    @JoinColumn(name = "profile_id")
    private List<Address> addresses = new ArrayList<>();

    @Convert(converter = JsonListConverter.class) // used to convert  the complex object into the database
    @Column(name = "education", columnDefinition = "TEXT")
    private List<Map<String, Object>> education = new ArrayList<>();

    @Convert(converter = JsonListConverter.class)
    @Column(name = "work_experience", columnDefinition = "TEXT")
    private List<Map<String, Object>> workExperience = new ArrayList<>();
}
