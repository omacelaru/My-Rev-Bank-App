package com.fmi.rev.app.security;

import com.fmi.rev.app.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtProvider {
    private static final String AUTHORITIES = "authorities";
    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(String username, Long ttl, Set<String> authorities) {
        Date expirationDateTime = Date.from(ZonedDateTime.now().plusMinutes(ttl).toInstant());
        return Jwts.builder()
                .setSubject(username)
                .claim(AUTHORITIES, authorities)
                .signWith(getJwtKey(), SignatureAlgorithm.HS512)
                .setExpiration(expirationDateTime)
                .compact();
    }

    public boolean validate(String token) {
        if (token.isBlank()) {
            throw new UnauthorizedException("Missing JWT");
        }
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getJwtKey())
                    .build()
                    .parseClaimsJws(token);
        } catch (JwtException e) {
            throw new UnauthorizedException("Invalid JWT");
        }
        return true;
    }

    public Authentication doAuthentication(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getJwtKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            Set<SimpleGrantedAuthority> grantedAuthorities = ((ArrayList<String>) claims.get(AUTHORITIES)).stream()
                    .filter(claim -> claim != null && !claim.isBlank())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());
            User user = new User(claims.getSubject(), "", grantedAuthorities);
            return new UsernamePasswordAuthenticationToken(user, "", grantedAuthorities);
        } catch (JwtException e) {
            throw new UnauthorizedException("Invalid JWT");
        }
    }

    private Key getJwtKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
