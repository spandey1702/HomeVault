package com.homevault.service;

import com.homevault.dto.ItemDto;
import com.homevault.entity.Item;
import com.homevault.entity.User;
import com.homevault.entity.Household;
import com.homevault.repository.ItemRepository;
import com.homevault.repository.UserRepository;
import com.homevault.repository.HouseholdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ItemService {
    
    @Autowired
    private ItemRepository itemRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private HouseholdRepository householdRepository;
    
    public List<ItemDto> getItemsByUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        List<Item> items = itemRepository.findByOwner(userOptional.get());
        return items.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        Item item = new Item(itemDto.getName(), userOptional.get());
        mapDtoToEntity(itemDto, item);
        
        if (itemDto.getHouseholdId() != null) {
            Optional<Household> householdOptional = householdRepository.findById(itemDto.getHouseholdId());
            householdOptional.ifPresent(item::setHousehold);
        }
        
        Item savedItem = itemRepository.save(item);
        return convertToDto(savedItem);
    }
    
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long userId) {
        Optional<Item> itemOptional = itemRepository.findById(itemId);
        if (itemOptional.isEmpty()) {
            throw new RuntimeException("Item not found");
        }
        
        Item item = itemOptional.get();
        if (!item.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to update this item");
        }
        
        mapDtoToEntity(itemDto, item);
        Item savedItem = itemRepository.save(item);
        return convertToDto(savedItem);
    }
    
    public void deleteItem(Long itemId, Long userId) {
        Optional<Item> itemOptional = itemRepository.findById(itemId);
        if (itemOptional.isEmpty()) {
            throw new RuntimeException("Item not found");
        }
        
        Item item = itemOptional.get();
        if (!item.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this item");
        }
        
        itemRepository.delete(item);
    }
    
    public List<ItemDto> getExpiringItems(Long userId, int daysAhead) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(daysAhead);
        
        List<Item> items = itemRepository.findExpiringItems(userOptional.get(), today, endDate);
        return items.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    private ItemDto convertToDto(Item item) {
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
        
        dto.setExpiring(item.isExpiring(7)); // 7 days ahead
        dto.setExpired(item.isExpired());
        
        return dto;
    }
    
    private void mapDtoToEntity(ItemDto dto, Item item) {
        if (dto.getName() != null) item.setName(dto.getName());
        if (dto.getDescription() != null) item.setDescription(dto.getDescription());
        if (dto.getCategory() != null) item.setCategory(dto.getCategory());
        if (dto.getLocation() != null) item.setLocation(dto.getLocation());
        if (dto.getQuantity() != null) item.setQuantity(dto.getQuantity());
        if (dto.getPrice() != null) item.setPrice(dto.getPrice());
        if (dto.getPurchaseDate() != null) item.setPurchaseDate(dto.getPurchaseDate());
        if (dto.getExpiryDate() != null) item.setExpiryDate(dto.getExpiryDate());
        if (dto.getBrand() != null) item.setBrand(dto.getBrand());
        if (dto.getModel() != null) item.setModel(dto.getModel());
        if (dto.getNotes() != null) item.setNotes(dto.getNotes());
        if (dto.getImageUrl() != null) item.setImageUrl(dto.getImageUrl());
    }
}