package com.lz.devflow.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {


    @Value("${security.jwt.secret-key}")
    private String secretKey;

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().verifyWith(getEncryptedKey()).build().parseSignedClaims(authToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private SecretKey getEncryptedKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims extractClaimsFromJwt(String jwt) {
        return Jwts.parser().verifyWith(getEncryptedKey()).build().parseSignedClaims(jwt).getPayload();
    }

    // generate a new jwt token with the given subject and role, role wil be set into audience, and expires in 2 years
    public String generateToken(String subject, String role) {
        return Jwts.builder().subject(subject).audience().add(role).and()
                .expiration(DateUtils.addDays(new Date(), 365))
                .signWith(getEncryptedKey()).compact();
    }
}
