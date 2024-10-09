package com.mycompany.homevault.controllers;

import com.mycompany.homevault.model.Item;
import com.mycompany.homevault.repo.ItemRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;

    @PostMapping("/add")
    public ResponseEntity<String> addItem(@RequestBody Item item, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return new ResponseEntity<>("User not logged in", HttpStatus.UNAUTHORIZED);
        }

        try {
            item.setUsername(username); // Ensure Item entity has a username attribute
            itemRepository.save(item);
            return new ResponseEntity<>("Item added successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error adding item: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user")
    public ResponseEntity<List<Item>> getItemsByUser(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        List<Item> items = itemRepository.findByUsername(username);
        return new ResponseEntity<>(items, HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateItem(@PathVariable Integer id, @RequestBody Item updatedItem, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return new ResponseEntity<>("User not logged in", HttpStatus.UNAUTHORIZED);
        }

        try {
            Item item = itemRepository.findById(id);
            if (item == null) {
                return new ResponseEntity<>("Item not found", HttpStatus.NOT_FOUND);
            }

            if (!item.getUsername().equals(username)) {
                return new ResponseEntity<>("Not authorized to update this item", HttpStatus.FORBIDDEN);
            }

            item.setName(updatedItem.getName());
            item.setCategory(updatedItem.getCategory());
            item.setDescription(updatedItem.getDescription());
            item.setPurchaseDate(updatedItem.getPurchaseDate());
            item.setValue(updatedItem.getValue());
            item.setExpiryDate(updatedItem.getExpiryDate());

            itemRepository.update(item);
            return new ResponseEntity<>("Item updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating item: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteItem(@PathVariable Integer id, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return new ResponseEntity<>("User not logged in", HttpStatus.UNAUTHORIZED);
        }

        try {
            Item item = itemRepository.findById(id);
            if (item == null) {
                return new ResponseEntity<>("Item not found", HttpStatus.NOT_FOUND);
            }

            if (!item.getUsername().equals(username)) {
                return new ResponseEntity<>("Not authorized to delete this item", HttpStatus.FORBIDDEN);
            }

            itemRepository.delete(item);
            return new ResponseEntity<>("Item deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting item: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
