package com.blindtest.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private JwtService jwtService;
    private final ObjectMapper mapper;

    private static final String[] excludedEndpoints = {"/users/login", "/users/create"};

    public JwtAuthorizationFilter(JwtService jwtService, ObjectMapper mapper) {
        this.jwtService = jwtService;
        this.mapper = mapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return Arrays.stream(excludedEndpoints)
                .anyMatch(e -> new AntPathMatcher().match(e, request.getServletPath()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            // Récupérer les claims du JWT s'il y en a un
            Claims claims = jwtService.resolveClaims(request);
    
            // Si on a des claims, on procède normalement
            if (claims != null) {
                String email = claims.getSubject();
                Boolean isGuest = claims.get("isGuest", Boolean.class);
                String userName = claims.get("userName", String.class);
    
                if (isGuest != null && isGuest && userName != null) {
                    // Si c'est un guest avec un username valide, on ne vérifie pas le token
                    System.out.println("Guest user detected: " + userName);
                    filterChain.doFilter(request, response);
                    return;
                } else {
                    // Traiter l'utilisateur normal avec JWT
                    System.out.println("Token valid for user: " + email);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(email, "", new ArrayList<>());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } else {
                // Pas de token, vérifie si on autorise les guests pour cet endpoint
                String endpoint = request.getRequestURI();
                if (isGuestAccessAllowed(endpoint)) {
                    System.out.println("Access allowed for guest to: " + endpoint);
                    filterChain.doFilter(request, response);
                    return;
                }
            }
    
        } catch (Exception e) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("Authentication Error: " + e.getMessage());
            return;
        }
    
        filterChain.doFilter(request, response);
    }

    private boolean isGuestAccessAllowed(String endpoint) {
        // Liste des API que les guests peuvent appeler sans authentification
        return endpoint.startsWith("/sessions/{sessionCode}/answer") || endpoint.startsWith("/guest"); 
    }

}
