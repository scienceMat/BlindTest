package com.blindtest.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import com.blindtest.dto.UserDTO;

import org.springframework.beans.factory.annotation.Value;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

@Service
public class JwtService {

    private final String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    // Injection de la clé secrète depuis application.properties
    public JwtService(@Value("${security.jwt.secret-key}") String secretKey,
    @Value("${security.jwt.expiration-time}") long jwtExpiration) {
this.secretKey = secretKey;
this.jwtExpiration = jwtExpiration;
}

    // Méthode pour extraire le token JWT du header Authorization
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

     public String generateToken(UserDTO userDTO) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userDTO.getId());
        claims.put("isAdmin", userDTO.getIsAdmin());
        return buildToken(claims, userDTO.getUserName(), jwtExpiration);
    }

    // Méthode pour résoudre les claims JWT du token
    public Claims resolveClaims(HttpServletRequest req) {
        try {
            String token = resolveToken(req);
            if (token != null) {
                return extractAllClaims(token);
            }
            return null;
        } catch (ExpiredJwtException ex) {
            req.setAttribute("expired", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            req.setAttribute("invalid", ex.getMessage());
            throw ex;
        }
    }

    // Méthode pour valider les claims du JWT
    public boolean validateClaims(Claims claims) {
        try {
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            throw new RuntimeException("Token expired or invalid", e);
        }
    }

    // Méthode pour extraire tous les claims du token
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

       private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignInKey() {
        // Décode la clé secrète encodée en base64
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        // Crée une clé HMAC SHA-256 à partir des octets de la clé décodée
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
