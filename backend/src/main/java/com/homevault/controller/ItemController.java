package com.homevault.controller;

import com.homevault.dto.ItemDto;
import com.homevault.service.ItemService;
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
@RequestMapping("/items")
public class ItemController {

    @Autowired private ItemService itemService;
    @Autowired private UserService userService;

    private Long resolveUserId(UserDetails principal) {
        return userService.findByEmail(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    @GetMapping
    public ResponseEntity<?> getItems(@AuthenticationPrincipal UserDetails principal) {
        try {
            List<ItemDto> items = itemService.getItemsByUser(resolveUserId(principal));
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Items visible to every member of the caller's household. */
    @GetMapping("/household")
    public ResponseEntity<?> getHouseholdItems(@AuthenticationPrincipal UserDetails principal) {
        try {
            List<ItemDto> items = itemService.getItemsByHousehold(resolveUserId(principal));
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createItem(@Valid @RequestBody ItemDto itemDto,
                                        @AuthenticationPrincipal UserDetails principal) {
        try {
            ItemDto created = itemService.createItem(itemDto, resolveUserId(principal));
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Long id,
                                        @Valid @RequestBody ItemDto itemDto,
                                        @AuthenticationPrincipal UserDetails principal) {
        try {
            ItemDto updated = itemService.updateItem(id, itemDto, resolveUserId(principal));
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Long id,
                                        @AuthenticationPrincipal UserDetails principal) {
        try {
            itemService.deleteItem(id, resolveUserId(principal));
            return ResponseEntity.ok(Map.of("message", "Item deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/expiring")
    public ResponseEntity<?> getExpiringItems(@RequestParam(defaultValue = "7") int days,
                                              @AuthenticationPrincipal UserDetails principal) {
        try {
            List<ItemDto> items = itemService.getExpiringItems(resolveUserId(principal), days);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
