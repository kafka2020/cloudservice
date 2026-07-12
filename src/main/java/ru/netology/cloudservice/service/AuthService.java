package ru.netology.cloudservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import ru.netology.cloudservice.dto.LoginRequest;
import ru.netology.cloudservice.dto.LoginResponse;
import ru.netology.cloudservice.security.JwtService;
import ru.netology.cloudservice.security.TokenBlacklist;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenBlacklist tokenBlacklist;

    public LoginResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.login(), request.password())
        );
        String token = jwtService.generateToken(auth.getName());
        return new LoginResponse(token);
    }

    public void logout(String token) {
        if (token == null) return;
        if (token.startsWith("Bearer ")) token = token.substring(7).trim();
        tokenBlacklist.revoke(token);
    }
}
