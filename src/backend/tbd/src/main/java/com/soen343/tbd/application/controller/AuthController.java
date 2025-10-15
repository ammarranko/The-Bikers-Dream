package com.soen343.tbd.application.controller;

import com.soen343.tbd.application.dto.LoginRequest;
import com.soen343.tbd.application.dto.LoginResponse;
import com.soen343.tbd.application.dto.SignupRequest;
import com.soen343.tbd.application.service.AuthService;
import com.soen343.tbd.infrastructure.security.JwtUtil;
import com.soen343.tbd.domain.repository.UserRepository;
import com.soen343.tbd.domain.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // Authenticate user
        if (authService.authenticate(loginRequest.getEmail(), loginRequest.getPassword())) {
            // Generate JWT token
            String token = jwtUtil.generateToken(loginRequest.getEmail());
            // Fetch user details
            User user = userRepository.findByEmail(loginRequest.getEmail()).orElse(null);
            String fullName = user.getFullName();
            LoginResponse response = new LoginResponse(token, loginRequest.getEmail(), fullName, user.getRole());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.ok(false);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody SignupRequest signupRequest) {
        // Check if email already exists
        if (userRepository.findByEmail(signupRequest.getEmail()).isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        // Register the user
        authService.registerUser(
            signupRequest.getFullName(),
            signupRequest.getEmail(),
            signupRequest.getPassword(),
            signupRequest.getAddress(),
            signupRequest.getUsername()
        );

        Map<String, String> response = new HashMap<>();
        response.put("message", "User registered successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
