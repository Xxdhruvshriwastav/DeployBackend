package com.example.demo;

import com.hireconnect.auth.dto.RegisterRequest;
import com.hireconnect.auth.entity.UserCredential;
import com.hireconnect.auth.repository.AuthRepository;
import com.hireconnect.auth.service.AuthServiceImpl;
import com.hireconnect.auth.util.JwtUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceApplicationTest {

    @Mock
    private AuthRepository authRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

// REGISTER TESTS
    @Test
    void testRegisterSuccess() {

        RegisterRequest request = new RegisterRequest("test@gmail.com", "123");

        when(authRepository.existsByEmail(request.getEmail())).thenReturn(false); // retuen false when this is, existsByEmail("test@gmail.com") when someone call this then it returns that this email is not register, we can register via this mail
        when(passwordEncoder.encode("123")).thenReturn("encodePass"); // not real hashing, just return fakevalue

        UserCredential savedUser = new UserCredential();

        savedUser.setEmail("test@gmail.com");

        when(authRepository.save(any(UserCredential.class))).thenReturn(savedUser); // save User return krega

        UserCredential result = authService.register(request, "CANDIDATE"); // register -> email, pass from object & pass

        assertEquals("test@gmail.com", result.getEmail());

        verify(authRepository).save(any(UserCredential.class)); // it verfies, doest it call authRepository.save()

    }

        // 2: FAIL
        @Test
        void testRegister_Fail_EmailExists () {
            RegisterRequest request = new RegisterRequest("test@gmail.com", "123");

            when(authRepository.existsByEmail("test@gmail.com")).thenReturn(true);

            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    authService.register(request, "CANDIDTE"));

            assertEquals("Email already registered", ex.getMessage());

        }

        @Test
    void testLoginSuccess() {

        UserCredential user = new UserCredential();
        user.setEmail("test@gmail.com");
        user.setPassword("encodedPass");
        user.setRole("CANDIDATE");


        when(authRepository.findByEmail("test@gmail.com")) // when user search in login then this returns
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("123", "encodedPass"))  // match with stored password
                .thenReturn(true);

            when(jwtUtil.generateToken("test@gmail.com", "CANDIDATE"))
                    .thenReturn("mock-token");

        String token = authService.login("test@gmail.com", "123");

        assertEquals("mock-token", token);

        }

        @Test
     void testLoginFail_UserNotFound(){

            when(authRepository.findByEmail("test@gmail.com"))
                    .thenReturn(Optional.empty());

                    assertThrows(RuntimeException.class,() ->

                            authService.login("test@gmail.com", "123")
                            );

        }

    @Test
    void testLoginFail_WrongPassword() {
        UserCredential user = new UserCredential();
        user.setPassword("encodedPass");

        when(authRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("123", "encodedPass"))
                .thenReturn(false);

        assertThrows(RuntimeException.class, () ->
                authService.login("test@gmail.com", "123"));
    }


    // tokenTest
    @Test
    void testValidateToken() {
        when(jwtUtil.isTokenValid("token")).thenReturn(true);

        boolean result = authService.validateToken("token"); // here validatetoken method calls isTokenValid

        assertTrue(result);
    }

    @Test
    void testRefreshToken() {
        when(jwtUtil.extractEmail("oldToken")).thenReturn("test@gmail.com");
        when(jwtUtil.extractRole("oldToken")).thenReturn("CANDIDATE");
        when(jwtUtil.generateToken("test@gmail.com", "CANDIDATE"))
                .thenReturn("newToken");

        String newToken = authService.refreshToken("oldToken");

        assertEquals("newToken", newToken);
    }

    // GetUser Tests

    @Test
    void testGetUserByEmail() {
        UserCredential user = new UserCredential();

        when(authRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        UserCredential result = authService.getUserByEmail("test@gmail.com");

        assertNotNull(result);
    }


    @Test
    void testGetUserById() {
        UserCredential user = new UserCredential();

        when(authRepository.findById(1))
                .thenReturn(Optional.of(user));

        UserCredential result = authService.getUserById(1);

        assertNotNull(result);
    }

    // get role

    @Test
    void testGetRoleFromToken() {

        when(jwtUtil.extractRole("token")).thenReturn("CANDIDATE");

        String role = authService.getRoleFromToken("token");

        assertEquals("CANDIDATE", role);
    }


}









