package ru.netology.cloudservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Читает auth-token из заголовка (поддерживается также формат "Bearer xxx",
 * именно так его отправляет фронт Нетологии), проверяет JWT и авторизует
 * пользователя в Spring Security.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    public static final String AUTH_HEADER = "auth-token";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklist blacklist;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(AUTH_HEADER);
        String token = stripBearer(header);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                if (blacklist.isRevoked(token)) {
                    log.debug("Token revoked");
                } else {
                    String login = jwtService.extractLogin(token);
                    if (login != null) {
                        UserDetails ud = userDetailsService.loadUserByUsername(login);
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(ud, token, ud.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (Exception ex) {
                log.debug("JWT validation failed: {}", ex.getMessage());
            }
        }
        chain.doFilter(request, response);
    }

    private String stripBearer(String header) {
        if (header == null || header.isBlank()) return null;
        if (header.startsWith("Bearer ")) return header.substring(7).trim();
        return header.trim();
    }
}
