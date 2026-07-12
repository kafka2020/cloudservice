package ru.netology.cloudservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.netology.cloudservice.dto.LoginRequest;
import ru.netology.cloudservice.dto.LoginResponse;
import ru.netology.cloudservice.security.JwtAuthFilter;
import ru.netology.cloudservice.service.AuthService;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(JwtAuthFilter.AUTH_HEADER) String token) {
        authService.logout(token);
        return ResponseEntity.ok().build();
    }
}
