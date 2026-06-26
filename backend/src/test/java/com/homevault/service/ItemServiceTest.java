package com.homevault.service;

import com.homevault.dto.ItemDto;
import com.homevault.entity.Item;
import com.homevault.entity.User;
import com.homevault.repository.HouseholdRepository;
import com.homevault.repository.ItemRepository;
import com.homevault.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock private ItemRepository itemRepository;
    @Mock private UserRepository userRepository;
    @Mock private HouseholdRepository householdRepository;

    @InjectMocks
    private ItemService itemService;

    private User owner;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = new User("Sagarika", "sagarika@example.com", "password123");
        owner.setId(1L);

        item = new Item("Laptop", owner);
        item.setId(10L);
    }

    @Test
    void createItem_savesItemWithCorrectOwner() {
        ItemDto dto = new ItemDto();
        dto.setName("Laptop");

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> {
            Item saved = inv.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        ItemDto result = itemService.createItem(dto, 1L);

        assertEquals("Laptop", result.getName());
        assertEquals(1L, result.getOwnerId());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void createItem_throwsWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ItemDto dto = new ItemDto();
        dto.setName("Gadget");

        assertThrows(RuntimeException.class, () -> itemService.createItem(dto, 99L));
        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateItem_throwsWhenCallerIsNotOwner() {
        User other = new User("Other", "other@example.com", "pass1234");
        other.setId(2L);

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        ItemDto dto = new ItemDto();
        dto.setName("Updated Name");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> itemService.updateItem(10L, dto, 2L));
        assertTrue(ex.getMessage().contains("authorised"));
        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateItem_savesChangesWhenOwnerMatches() {
        ItemDto dto = new ItemDto();
        dto.setName("Updated Laptop");

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.updateItem(10L, dto, 1L);

        assertEquals("Updated Laptop", result.getName());
        verify(itemRepository).save(item);
    }

    @Test
    void deleteItem_throwsWhenCallerIsNotOwner() {
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        assertThrows(RuntimeException.class, () -> itemService.deleteItem(10L, 2L));
        verify(itemRepository, never()).delete(any());
    }

    @Test
    void deleteItem_deletesWhenOwnerMatches() {
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        itemService.deleteItem(10L, 1L);

        verify(itemRepository).delete(item);
    }

    @Test
    void getItemsByUser_returnsEmptyListWhenUserHasNoItems() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwner(owner)).thenReturn(List.of());

        List<ItemDto> result = itemService.getItemsByUser(1L);

        assertTrue(result.isEmpty());
    }
}
