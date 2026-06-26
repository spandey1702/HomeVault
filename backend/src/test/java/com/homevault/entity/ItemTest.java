package com.homevault.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class ItemTest {

    @Test
    void isExpired_returnsFalse_whenNoExpiryDate() {
        Item item = new Item();
        assertFalse(item.isExpired());
    }

    @Test
    void isExpired_returnsTrue_whenExpiryDateIsInPast() {
        Item item = new Item();
        item.setExpiryDate(LocalDate.now().minusDays(1));
        assertTrue(item.isExpired());
    }

    @Test
    void isExpired_returnsFalse_whenExpiryDateIsToday() {
        Item item = new Item();
        item.setExpiryDate(LocalDate.now());
        assertFalse(item.isExpired());
    }

    @Test
    void isExpiring_returnsFalse_whenNoExpiryDate() {
        Item item = new Item();
        assertFalse(item.isExpiring(7));
    }

    @Test
    void isExpiring_returnsTrue_whenExpiryDateIsWithinWindow() {
        Item item = new Item();
        item.setExpiryDate(LocalDate.now().plusDays(3));
        assertTrue(item.isExpiring(7));
    }

    @Test
    void isExpiring_returnsFalse_whenExpiryDateIsBeyondWindow() {
        Item item = new Item();
        item.setExpiryDate(LocalDate.now().plusDays(10));
        assertFalse(item.isExpiring(7));
    }

    @Test
    void isExpiring_returnsTrue_whenItemIsAlreadyExpired() {
        Item item = new Item();
        item.setExpiryDate(LocalDate.now().minusDays(1));
        assertTrue(item.isExpiring(7));
    }
}
