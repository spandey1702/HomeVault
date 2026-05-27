package com.homevault.service;

import com.homevault.dto.DashboardStatsDto;
import com.homevault.dto.ItemDto;
import com.homevault.dto.ReminderDto;
import com.homevault.entity.Household;
import com.homevault.entity.Item;
import com.homevault.entity.Reminder;
import com.homevault.entity.User;
import com.homevault.repository.HouseholdRepository;
import com.homevault.repository.ItemRepository;
import com.homevault.repository.ReminderRepository;
import com.homevault.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired private UserRepository userRepository;
    @Autowired private HouseholdRepository householdRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private ReminderRepository reminderRepository;
    @Autowired private ItemService itemService;
    @Autowired private ReminderService reminderService;

    public DashboardStatsDto getStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Household> households = householdRepository.findByMemberId(userId);

        LocalDate now   = LocalDate.now();
        LocalDate in7   = now.plusDays(7);

        // Aggregate all items visible to this user across all their households
        List<Item> allItems = households.stream()
                .flatMap(h -> itemRepository.findByHousehold(h).stream())
                .collect(Collectors.toList());

        long total    = allItems.size();
        long expiring = allItems.stream().filter(i -> i.isExpiring(7) && !i.isExpired()).count();
        long expired  = allItems.stream().filter(Item::isExpired).count();
        long members  = households.stream()
                .flatMap(h -> h.getMembers().stream())
                .map(User::getId).distinct().count();

        // Pending reminders across all households
        long pendingReminders = reminderRepository.findAllForUser(user).stream()
                .filter(r -> r.getStatus() == Reminder.Status.PENDING)
                .count();

        // Top 5 soonest-expiring items (not yet expired)
        List<ItemDto> recentlyExpiring = allItems.stream()
                .filter(i -> !i.isExpired() && i.getExpiryDate() != null
                             && !i.getExpiryDate().isBefore(now))
                .sorted(Comparator.comparing(Item::getExpiryDate))
                .limit(5)
                .map(itemService::toDto)
                .collect(Collectors.toList());

        // Top 5 upcoming pending reminders
        List<ReminderDto> upcomingReminders = reminderRepository.findAllForUser(user).stream()
                .filter(r -> r.getStatus() == Reminder.Status.PENDING)
                .limit(5)
                .map(reminderService::toDto)
                .collect(Collectors.toList());

        DashboardStatsDto stats = new DashboardStatsDto();
        stats.setTotalItems(total);
        stats.setExpiringItems(expiring);
        stats.setExpiredItems(expired);
        stats.setTotalMembers(members);
        stats.setPendingReminders(pendingReminders);
        stats.setRecentlyExpiring(recentlyExpiring);
        stats.setUpcomingReminders(upcomingReminders);
        return stats;
    }
}
