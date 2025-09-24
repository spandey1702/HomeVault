package com.homevault.controller;

import com.homevault.dto.ItemDto;
import com.homevault.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/items")
@CrossOrigin(origins = "*")
public class ItemController {
    
    @Autowired
    private ItemService itemService;
    
    @GetMapping
    public ResponseEntity<?> getItems(@RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            List<ItemDto> items = itemService.getItemsByUser(userId);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createItem(@Valid @RequestBody ItemDto itemDto, 
                                       @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            ItemDto createdItem = itemService.createItem(itemDto, userId);
            return ResponseEntity.ok(createdItem);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Long id, 
                                       @Valid @RequestBody ItemDto itemDto,
                                       @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            ItemDto updatedItem = itemService.updateItem(id, itemDto, userId);
            return ResponseEntity.ok(updatedItem);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Long id,
                                       @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            itemService.deleteItem(id, userId);
            return ResponseEntity.ok(Map.of("message", "Item deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/expiring")
    public ResponseEntity<?> getExpiringItems(@RequestParam(defaultValue = "7") int days,
                                             @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            List<ItemDto> items = itemService.getExpiringItems(userId, days);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    private Long extractUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer temp_token_")) {
            throw new RuntimeException("Invalid authorization header");
        }
        
        String token = authHeader.substring(7); // Remove "Bearer "
        String userIdStr = token.replace("temp_token_", "");
        return Long.parseLong(userIdStr);
    }
}