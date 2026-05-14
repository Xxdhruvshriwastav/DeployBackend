package com.hireconnect.auth.resource;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import com.hireconnect.auth.dto.AuthRequest;
import com.hireconnect.auth.dto.AuthResponse;
import com.hireconnect.auth.dto.RegisterRequest;
import com.hireconnect.auth.entity.UserCredential;
import com.hireconnect.auth.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthResource {

    private final AuthService authService;

    // Candidate Register
    @PostMapping("/register/candidate")
    public ResponseEntity<?> registerCandidate(@RequestBody RegisterRequest request) {
        try {
            authService.register(request, "CANDIDATE");
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Candidate registered successfully", "role", "CANDIDATE"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // Recruiter Register
    @PostMapping("/register/recruiter")
    public ResponseEntity<?> registerRecruiter(@RequestBody RegisterRequest request) {
        try {
            authService.register(request, "RECRUITER");
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Recruiter registered successfully", "role", "RECRUITER"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // Generic Register
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request, @RequestParam String role) {
        try {
            authService.register(request, role);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", role + " registered successfully", "role", role));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // Login — dono ke liye same
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            String token = authService.login(request.getEmail(), request.getPassword());
            UserCredential user = authService.getUserByEmail(request.getEmail());
            return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .role(user.getRole())
                .userId(user.getUserId())
                .email(user.getEmail())
                .message("Login successful")
                .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> request) {
        System.out.println("Received Google login request...");
        try {
            String idToken = request.get("idToken");
            if (idToken == null || idToken.isEmpty()) {
                System.err.println("idToken is NULL or EMPTY in request body");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of("message", "idToken is missing in request body"));
            }
            AuthResponse response = authService.googleLogin(idToken);
            System.out.println("Google login service call successful.");
            return ResponseEntity.ok(response);
        } catch (Throwable e) {
            System.err.println("CRITICAL ERROR in AuthResource.googleLogin: " + e.getMessage());
            e.printStackTrace();
            java.util.Map<String, String> errorResponse = new java.util.HashMap<>();
            errorResponse.put("message", "Backend Error: " + e.getMessage());
            errorResponse.put("type", e.getClass().getName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestHeader("Authorization") String authHeader) {
        authService.logout(authHeader.replace("Bearer ", ""));
        return ResponseEntity.ok("Logged out");
    }

    @GetMapping("/validate")
    public ResponseEntity<Boolean> validate(
            @RequestHeader("Authorization") String authHeader) {
        boolean valid = authService.validateToken(
                authHeader.replace("Bearer ", ""));
        return ResponseEntity.ok(valid);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(
            @RequestHeader("Authorization") String authHeader) {
        String newToken = authService.refreshToken(
                authHeader.replace("Bearer ", ""));
        return ResponseEntity.ok(Map.of("token", newToken));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<UserCredential> getUserById(@PathVariable int id) {
        return ResponseEntity.ok(authService.getUserById(id));
    }
}