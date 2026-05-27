package com.homevault.service;

import com.homevault.dto.ItemDto;
import com.homevault.entity.Household;
import com.homevault.entity.Item;
import com.homevault.entity.User;
import com.homevault.repository.HouseholdRepository;
import com.homevault.repository.ItemRepository;
import com.homevault.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ItemService {

    @Autowired private ItemRepository itemRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private HouseholdRepository householdRepository;

    // ── Queries ───────────────────────────────────────────────────────────────

    public List<ItemDto> getItemsByUser(Long userId) {
        User user = requireUser(userId);
        return itemRepository.findByOwner(user).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    /** Return all items belonging to every household the user is a member of. */
    public List<ItemDto> getItemsByHousehold(Long userId) {
        User user = requireUser(userId);
        return user.getHouseholds().stream()
                .flatMap(h -> h.getItems().stream())
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<ItemDto> getExpiringItems(Long userId, int daysAhead) {
        User user = requireUser(userId);
        LocalDate end = LocalDate.now().plusDays(daysAhead);
        return itemRepository.findExpiringItems(user, LocalDate.now(), end).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    /** All items expiring within {@code daysAhead} days across the whole app — used by the scheduler. */
    public List<Item> getAllExpiringItemsRaw(int daysAhead) {
        LocalDate end = LocalDate.now().plusDays(daysAhead);
        return itemRepository.findAllExpiringItems(LocalDate.now(), end);
    }

    // ── Mutations ─────────────────────────────────────────────────────────────

    public ItemDto createItem(ItemDto dto, Long userId) {
        User user = requireUser(userId);
        Item item = new Item(dto.getName(), user);
        applyDto(dto, item);
        if (dto.getHouseholdId() != null) {
            householdRepository.findById(dto.getHouseholdId()).ifPresent(item::setHousehold);
        }
        return toDto(itemRepository.save(item));
    }

    public ItemDto updateItem(Long itemId, ItemDto dto, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        if (!item.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Not authorised to update this item");
        }
        applyDto(dto, item);
        return toDto(itemRepository.save(item));
    }

    public void deleteItem(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        if (!item.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Not authorised to delete this item");
        }
        itemRepository.delete(item);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void applyDto(ItemDto dto, Item item) {
        if (dto.getName()         != null) item.setName(dto.getName());
        if (dto.getDescription()  != null) item.setDescription(dto.getDescription());
        if (dto.getCategory()     != null) item.setCategory(dto.getCategory());
        if (dto.getLocation()     != null) item.setLocation(dto.getLocation());
        if (dto.getQuantity()     != null) item.setQuantity(dto.getQuantity());
        if (dto.getPrice()        != null) item.setPrice(dto.getPrice());
        if (dto.getPurchaseDate() != null) item.setPurchaseDate(dto.getPurchaseDate());
        if (dto.getExpiryDate()   != null) item.setExpiryDate(dto.getExpiryDate());
        if (dto.getBrand()        != null) item.setBrand(dto.getBrand());
        if (dto.getModel()        != null) item.setModel(dto.getModel());
        if (dto.getNotes()        != null) item.setNotes(dto.getNotes());
        if (dto.getImageUrl()     != null) item.setImageUrl(dto.getImageUrl());
    }

    ItemDto toDto(Item item) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setCategory(item.getCategory());
        dto.setLocation(item.getLocation());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setPurchaseDate(item.getPurchaseDate());
        dto.setExpiryDate(item.getExpiryDate());
        dto.setBrand(item.getBrand());
        dto.setModel(item.getModel());
        dto.setNotes(item.getNotes());
        dto.setImageUrl(item.getImageUrl());
        dto.setOwnerId(item.getOwner().getId());
        dto.setOwnerName(item.getOwner().getName());
        if (item.getHousehold() != null) {
            dto.setHouseholdId(item.getHousehold().getId());
            dto.setHouseholdName(item.getHousehold().getName());
        }
        dto.setExpiring(item.isExpiring(7));
        dto.setExpired(item.isExpired());
        return dto;
    }
}
