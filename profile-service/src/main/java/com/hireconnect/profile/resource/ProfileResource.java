package com.hireconnect.profile.resource;

import com.hireconnect.profile.dto.CandidateProfileDTO;
import com.hireconnect.profile.dto.RecruiterProfileDTO;
import com.hireconnect.profile.dto.UserProfileDTO;
import com.hireconnect.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class ProfileResource {

    private static final Logger log = LoggerFactory.getLogger(ProfileResource.class);

    private final ProfileService profileService;

    @GetMapping("/{email:.+}")
    public ResponseEntity<UserProfileDTO> getProfile(@PathVariable String email) {
        try {
            return ResponseEntity.ok(profileService.getProfileByEmail(email));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/candidate")
    public ResponseEntity<CandidateProfileDTO> createCandidateProfile(@RequestBody CandidateProfileDTO profile) {
        return ResponseEntity.status(HttpStatus.CREATED).body(profileService.addCandidateProfile(profile));
    }

    @PostMapping("/recruiter")
    public ResponseEntity<RecruiterProfileDTO> createRecruiterProfile(@RequestBody RecruiterProfileDTO profile) {
        return ResponseEntity.status(HttpStatus.CREATED).body(profileService.addRecruiterProfile(profile));
    }

    @PutMapping("/candidate/{email}")
    public ResponseEntity<?> updateCandidateProfile(@PathVariable String email, @RequestBody CandidateProfileDTO profile) {
        try {
            return ResponseEntity.ok(profileService.updateProfile(email, profile));
        } catch (Exception e) {
            log.error("[UPDATE CANDIDATE] Failed for email={}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Update failed: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    @PutMapping("/recruiter/{email}")
    public ResponseEntity<?> updateRecruiterProfile(@PathVariable String email, @RequestBody RecruiterProfileDTO profile) {
        try {
            return ResponseEntity.ok(profileService.updateProfile(email, profile));
        } catch (Exception e) {
            log.error("[UPDATE RECRUITER] Failed for email={}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Update failed: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteProfile(@PathVariable String email) {
        profileService.deleteProfile(email);
        return ResponseEntity.noContent().build();
    }
}
