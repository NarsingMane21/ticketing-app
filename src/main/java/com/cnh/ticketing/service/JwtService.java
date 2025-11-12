package com.cnh.ticketing.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {
	private final Key key;
	private final long expirationMillis;

	public JwtService(@Value("${app.jwt-secret}") String secret,
			@Value("${app.jwt-expiration-ms}") long expirationMillis) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes());
		this.expirationMillis = expirationMillis;
	}

	public String generateToken(UserDetails userDetails) {
		Date now = new Date();
		Date exp = new Date(now.getTime() + expirationMillis);
		return Jwts.builder().setSubject(userDetails.getUsername())
				.claim("roles", userDetails.getAuthorities().stream().map(Object::toString).toList()).setIssuedAt(now)
				.setExpiration(exp).signWith(key).compact();
	}

	public String extractUsername(String token) {
		try {
			return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
		} catch (JwtException ex) {
			return null;
		}
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		String username = extractUsername(token);
		return username != null && username.equals(userDetails.getUsername());
	}
}