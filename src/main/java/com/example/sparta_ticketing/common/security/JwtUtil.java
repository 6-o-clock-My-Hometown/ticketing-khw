package com.example.sparta_ticketing.common.security;

import com.example.sparta_ticketing.common.exception.InvalidRequestException;
import com.example.sparta_ticketing.domain.user.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24시간

    @Value("${spring.jwt.secret.key}")
    private String secretKey;
    private Key key;
    //private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    public String createToken(Long id, String email, String nickname, UserRole userRole) {
        Date date = new Date();

        return BEARER_PREFIX + Jwts.builder()
                .setSubject(String.valueOf(id))
                .claim("email", email)
                .claim("nickname", nickname)
                .claim("userRole", userRole)
                .setExpiration(new Date(date.getTime() + EXPIRATION_TIME))
                .setIssuedAt(date)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String substringToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        throw new InvalidRequestException("토큰의 형식이 올바르지 않습니다");
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
