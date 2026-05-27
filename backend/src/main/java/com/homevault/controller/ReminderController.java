package com.homevault.controller;

import com.homevault.dto.ReminderDto;
import com.homevault.service.ReminderService;
import com.homevault.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/reminders")
public class ReminderController {

    @Autowired private ReminderService reminderService;
    @Autowired private UserService userService;

    private Long uid(UserDetails p) {
        return userService.findByEmail(p.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
    }

    @GetMapping
    public ResponseEntity<?> list(@AuthenticationPrincipal UserDetails p) {
        try {
            return ResponseEntity.ok(reminderService.getRemindersForUser(uid(p)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ReminderDto dto,
                                    @AuthenticationPrincipal UserDetails p) {
        try {
            return ResponseEntity.ok(reminderService.createReminder(dto, uid(p)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody ReminderDto dto,
                                    @AuthenticationPrincipal UserDetails p) {
        try {
            return ResponseEntity.ok(reminderService.updateReminder(id, dto, uid(p)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/done")
    public ResponseEntity<?> markDone(@PathVariable Long id,
                                      @AuthenticationPrincipal UserDetails p) {
        try {
            return ResponseEntity.ok(reminderService.markDone(id, uid(p)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails p) {
        try {
            reminderService.deleteReminder(id, uid(p));
            return ResponseEntity.ok(Map.of("message", "Reminder deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
