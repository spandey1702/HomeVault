package com.homevault.controller;

import com.homevault.dto.AuthResponse;
import com.homevault.dto.LoginRequest;
import com.homevault.dto.RegisterRequest;
import com.homevault.entity.User;
import com.homevault.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.createUser(request.getName(), request.getEmail(), request.getPassword());
            
            // Generate a simple token (in production, use JWT)
            String token = "temp_token_" + user.getId();
            
            AuthResponse response = new AuthResponse(token, user.getId(), user.getName(), user.getEmail());
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Optional<User> userOptional = userService.findByEmail(request.getEmail());
            
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid email or password"));
            }
            
            User user = userOptional.get();
            
            if (!userService.validatePassword(request.getPassword(), user.getPassword())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid email or password"));
            }
            
            // Generate a simple token (in production, use JWT)
            String token = "temp_token_" + user.getId();
            
            AuthResponse response = new AuthResponse(token, user.getId(), user.getName(), user.getEmail());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Login failed"));
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            // Simple token validation (in production, use JWT)
            if (authHeader == null || !authHeader.startsWith("Bearer temp_token_")) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            
            String token = authHeader.substring(7); // Remove "Bearer "
            String userIdStr = token.replace("temp_token_", "");
            Long userId = Long.parseLong(userIdStr);
            
            Optional<User> userOptional = userService.findById(userId);
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("error", "User not found"));
            }
            
            User user = userOptional.get();
            AuthResponse response = new AuthResponse(token, user.getId(), user.getName(), user.getEmail());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }
    }
}