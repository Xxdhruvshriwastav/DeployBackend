package com.hireconnect.auth.service;

import com.hireconnect.auth.entity.UserCredential;
import com.hireconnect.auth.dto.AuthResponse;
import com.hireconnect.auth.dto.RegisterRequest;

public interface AuthService {
    UserCredential register(RegisterRequest registerRequest, String role);
    String login(String email, String password);
    void logout(String token);
    boolean validateToken(String token);
    String refreshToken(String token);
    String getRoleFromToken(String token);
    AuthResponse googleLogin(String idToken);
    UserCredential getUserByEmail(String email);
    UserCredential getUserById(int userId);
    java.util.List<UserCredential> getAllUsers();
    UserCredential suspendUser(int userId, boolean suspend);
    UserCredential updateUser(int userId, UserCredential userDetails);
    void deleteUser(int userId);
}