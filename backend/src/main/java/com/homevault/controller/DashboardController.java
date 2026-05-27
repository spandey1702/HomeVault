package com.homevault.controller;

import com.homevault.service.DashboardService;
import com.homevault.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired private DashboardService dashboardService;
    @Autowired private UserService userService;

    @GetMapping("/stats")
    public ResponseEntity<?> stats(@AuthenticationPrincipal UserDetails p) {
        try {
            Long userId = userService.findByEmail(p.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found")).getId();
            return ResponseEntity.ok(dashboardService.getStats(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
