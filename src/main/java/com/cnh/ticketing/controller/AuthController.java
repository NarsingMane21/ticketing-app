package com.cnh.ticketing.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cnh.ticketing.dto.AuthResponse;
import com.cnh.ticketing.dto.LoginRequest;
import com.cnh.ticketing.dto.RegisterRequest;
import com.cnh.ticketing.model.User;
import com.cnh.ticketing.repository.UserRepository;
import com.cnh.ticketing.security.UserPrincipal;
import com.cnh.ticketing.service.JwtService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authManager;

	public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
			AuthenticationManager authManager) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.authManager = authManager;
	}

	@PostMapping("/register")
	public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
		if (!req.getPassword().equals(req.getConfirmPassword())) {
			return ResponseEntity.badRequest().body("Passwords do not match");
		}
		if (userRepository.existsByEmail(req.getEmail())) {
			return ResponseEntity.badRequest().body("Email already taken");
		}
		User user = User.builder().email(req.getEmail()).password(passwordEncoder.encode(req.getPassword()))
				.name(req.getName()).gender(req.getGender()).role(com.cnh.ticketing.model.Role.ROLE_USER).build();
		userRepository.save(user);
		var token = jwtService.generateToken(new UserPrincipal(user));
		return ResponseEntity.ok(new AuthResponse(token));
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
		Authentication auth = authManager
				.authenticate(new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
		UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
		var token = jwtService.generateToken(principal);
		return ResponseEntity.ok(new AuthResponse(token));
	}
}
