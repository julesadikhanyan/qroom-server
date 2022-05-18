package com.qroom.common.security.managers;

import com.qroom.common.security.UserAuth;
import com.qroom.common.security.config.SecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Component
public class JwtTokenManager {
    public JwtTokenManager(SecurityProperties properties) {
        this.properties = properties;
    }

    private enum TokenType {
        ACCESS, REFRESH
    }

    private final SecurityProperties properties;

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(properties.getSecret())
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private String getIdFromToken(String token) {
        return getClaimFromToken(token, Claims::getId);
    }

    private String getUserNameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    private String getTokenType(String token){
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("type", String.class);
    }

    public UserAuth readAccessToken(String token) {
        UUID uuid = UUID.fromString(getIdFromToken(token));
        String username = getUserNameFromToken(token);
        String type = getTokenType(token);
        if (!TokenType.ACCESS.name().toLowerCase(Locale.getDefault()).equals(type)) {
            throw new IllegalArgumentException("Token is not of ACCESS type");
        }

        List<SimpleGrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority("ACCESS"));

        return new UserAuth(uuid, username, token, roles);
    }

    public UserAuth readRefreshToken(String token){
        UUID uuid = UUID.fromString(getIdFromToken(token));
        String username = getUserNameFromToken(token);
        String type = getTokenType(token);
        if (!TokenType.REFRESH.name().toLowerCase(Locale.getDefault()).equals(type)) {
            throw new IllegalArgumentException("Token is not of REFRESH type");
        }

        List<SimpleGrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority("REFRESH"));

        return new UserAuth(uuid, username, token, roles);
    }

    public String generateAccessToken(UserAuth userAuth) {
        return deGenerateToken(userAuth, TokenType.ACCESS, properties.getTokenLifeTime());
    }

    public String generateRefreshToken(UserAuth userAuth) {
        return deGenerateToken(userAuth, TokenType.REFRESH, properties.getRefreshTokenLifeTime());
    }

    private String deGenerateToken(UserAuth userAuth, TokenType type, Duration tokenTTL) {
        return Jwts.builder()
                .claim("type", type.name().toLowerCase(Locale.getDefault()))
                .setId(userAuth.getUuid().toString())
                .setSubject(userAuth.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(tokenTTL)))
                .claim("roles", userAuth.getAuthorities().stream().map((Function<GrantedAuthority, Object>) GrantedAuthority::getAuthority))
                .signWith(SignatureAlgorithm.HS512, properties.getSecret())
                .compact();
    }
}
