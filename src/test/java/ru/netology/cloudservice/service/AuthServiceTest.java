package ru.netology.cloudservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import ru.netology.cloudservice.dto.LoginRequest;
import ru.netology.cloudservice.dto.LoginResponse;
import ru.netology.cloudservice.security.JwtService;
import ru.netology.cloudservice.security.TokenBlacklist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock JwtService jwtService;
    @Mock TokenBlacklist tokenBlacklist;

    @InjectMocks AuthService authService;

    @Test
    void login_returnsToken() {
        Authentication auth = new UsernamePasswordAuthenticationToken("user1", "pwd");
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtService.generateToken("user1")).thenReturn("jwt-token");

        LoginResponse resp = authService.login(new LoginRequest("user1", "pwd"));

        assertThat(resp.authToken()).isEqualTo("jwt-token");
    }

    @Test
    void logout_revokesToken() {
        authService.logout("Bearer abc");
        verify(tokenBlacklist).revoke("abc");
    }
}
