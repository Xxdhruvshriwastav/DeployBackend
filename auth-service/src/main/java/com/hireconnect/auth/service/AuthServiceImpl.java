package com.hireconnect.auth.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.hireconnect.auth.entity.UserCredential;
import com.hireconnect.auth.repository.AuthRepository;
import com.hireconnect.auth.util.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import com.hireconnect.auth.dto.AuthResponse;
import com.hireconnect.auth.dto.RegisterRequest;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${google.client.id}")
    private String googleClientId;

    @Override
    public UserCredential register(RegisterRequest registerRequest, String role) {
        if (authRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        UserCredential user = new UserCredential();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(role);
        user.setProvider("LOCAL");
        return authRepository.save(user);
    }

    @Override
    public String login(String email, String password) {
        UserCredential user = authRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return jwtUtil.generateToken(user.getEmail(), user.getRole());
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtil.isTokenValid(token);
    }

    @Override
    public String refreshToken(String token) {
        String email = jwtUtil.extractEmail(token);
        String role = jwtUtil.extractRole(token);
        return jwtUtil.generateToken(email, role);
    }
    
    @Override
    public String getRoleFromToken(String token) {
        return jwtUtil.extractRole(token);
    }

    @Override
    public AuthResponse googleLogin(String idToken) {
        if (idToken == null || idToken.isEmpty()) {
            throw new RuntimeException("ID Token is missing. Please ensure your Google Origin settings are correct.");
        }
        try {

            
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken == null) {
                throw new RuntimeException("Invalid Google ID Token (Verification failed)");
            }
            
            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            String email = payload.getEmail();
            
            System.out.println("Google login successful for: " + email);
            
            Optional<UserCredential> userOpt = authRepository.findByEmail(email);
            UserCredential user;

            if (userOpt.isPresent()) {
                user = userOpt.get();

            } else {
                // Naya user banao agar nahi hai (Default Candidate)
                user = new UserCredential();
                user.setEmail(email);
                user.setRole("CANDIDATE");
                user.setProvider("GOOGLE");
                user.setPassword(passwordEncoder.encode("GOOGLE_AUTH_DUMMY_" + System.currentTimeMillis()));
                user = authRepository.save(user);
            }
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
            
            return AuthResponse.builder()
                    .token(token)
                    .role(user.getRole())
                    .userId(user.getUserId())
                    .email(user.getEmail())
                    .message("Login successful via Google")
                    .build();
        } catch (Exception e) {
            System.err.println("Google Auth Error (Modern): " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Google verification failed: " + e.getMessage());
        }
    }

    @Override
    public void logout(String token) {
        // Redis blacklist — baad mein
    }

    @Override
    public UserCredential getUserByEmail(String email) {
        return authRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public UserCredential getUserById(int userId) {
        return authRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public java.util.List<UserCredential> getAllUsers() {
        return authRepository.findAll();
    }

    @Override
    public UserCredential suspendUser(int userId, boolean suspend) {
        UserCredential user = getUserById(userId);
        user.setActive(!suspend);
        return authRepository.save(user);
    }

    @Override
    public UserCredential updateUser(int userId, UserCredential userDetails) {
        UserCredential user = getUserById(userId);
        user.setRole(userDetails.getRole());
        user.setEmail(userDetails.getEmail());
        user.setActive(userDetails.isActive());
        return authRepository.save(user);
    }

    @Override
    public void deleteUser(int userId) {
        authRepository.deleteById(userId);
    }
}
