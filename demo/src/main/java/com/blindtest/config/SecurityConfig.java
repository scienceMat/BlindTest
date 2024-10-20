package com.blindtest.config;

import com.blindtest.auth.JwtAuthorizationFilter;
import com.blindtest.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthorizationFilter jwtAuthorizationFilter;
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(JwtAuthorizationFilter jwtAuthorizationFilter, CustomUserDetailsService customUserDetailsService) {
        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
        this.customUserDetailsService = customUserDetailsService;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())  // Désactiver CSRF pour une API stateless
            .cors(cors -> cors.and())      // Autoriser les requêtes CORS
            .authorizeHttpRequests(auth -> auth
                // Routes publiques : accès sans authentification
                .requestMatchers("/users/login", "/users/create", "/sessions/{sessionCode}/join", "/sessions/{sessionCode}/join-as-guest","/sessions/**","/sessions/code/{sessionCode}","/sessions/{sessionCode}/answer").permitAll()
                .requestMatchers("/chat/**").permitAll()
                // Toutes les autres routes nécessitent une authentification
                .anyRequest().authenticated()
            )
            // Désactiver la gestion des sessions (stateless, car utilisation de JWT)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // Ajouter le filtre JWT avant le UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
    
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(new BCryptPasswordEncoder()) // Si vous voulez définir ici
                .and()
                .build();
    }
}
