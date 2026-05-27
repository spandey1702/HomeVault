package com.homevault.controller;

import com.homevault.dto.HouseholdDto;
import com.homevault.service.HouseholdService;
import com.homevault.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/households")
public class HouseholdController {

    @Autowired private HouseholdService householdService;
    @Autowired private UserService userService;

    private Long uid(UserDetails p) {
        return userService.findByEmail(p.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
    }

    @GetMapping
    public ResponseEntity<?> list(@AuthenticationPrincipal UserDetails p) {
        try {
            List<HouseholdDto> list = householdService.getHouseholdsForUser(uid(p));
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails p) {
        try {
            return ResponseEntity.ok(householdService.getHousehold(id, uid(p)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody HouseholdDto dto,
                                    @AuthenticationPrincipal UserDetails p) {
        try {
            HouseholdDto created = householdService.createHousehold(
                    dto.getName(), dto.getDescription(), uid(p));
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Invite an existing user by email. */
    @PostMapping("/{id}/invite")
    public ResponseEntity<?> invite(@PathVariable Long id,
                                    @RequestBody Map<String, String> body,
                                    @AuthenticationPrincipal UserDetails p) {
        try {
            String email = body.get("email");
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "email is required"));
            }
            HouseholdDto updated = householdService.inviteMemberByEmail(id, email, uid(p));
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Remove a member (owner only). */
    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<?> removeMember(@PathVariable Long id,
                                          @PathVariable Long memberId,
                                          @AuthenticationPrincipal UserDetails p) {
        try {
            householdService.removeMember(id, memberId, uid(p));
            return ResponseEntity.ok(Map.of("message", "Member removed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Authenticated user leaves the household. */
    @DeleteMapping("/{id}/leave")
    public ResponseEntity<?> leave(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetails p) {
        try {
            householdService.leaveHousehold(id, uid(p));
            return ResponseEntity.ok(Map.of("message", "Left household"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails p) {
        try {
            householdService.deleteHousehold(id, uid(p));
            return ResponseEntity.ok(Map.of("message", "Household deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
