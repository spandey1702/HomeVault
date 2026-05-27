package com.homevault.controller;

import com.homevault.dto.AuthResponse;
import com.homevault.dto.LoginRequest;
import com.homevault.dto.RegisterRequest;
import com.homevault.entity.User;
import com.homevault.security.JwtUtil;
import com.homevault.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired private UserService userService;
    @Autowired private JwtUtil jwtUtil;

    /** Register a new user and return a signed JWT immediately. */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        try {
            User user = userService.createUser(req.getName(), req.getEmail(), req.getPassword());
            String token = jwtUtil.generateToken(user.getEmail());
            return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getName(), user.getEmail()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Authenticate with email + password and return a signed JWT. */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            User user = userService.findByEmail(req.getEmail())
                    .orElseThrow(() -> new RuntimeException("Invalid email or password"));

            if (!userService.validatePassword(req.getPassword(), user.getPassword())) {
                throw new RuntimeException("Invalid email or password");
            }

            String token = jwtUtil.generateToken(user.getEmail());
            return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getName(), user.getEmail()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    /** Return the currently authenticated user's profile (token already validated by filter). */
    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails principal) {
        return userService.findByEmail(principal.getUsername())
                .map(u -> ResponseEntity.ok(
                        new AuthResponse(null, u.getId(), u.getName(), u.getEmail())))
                .orElse(ResponseEntity.status(401).build());
    }
}
