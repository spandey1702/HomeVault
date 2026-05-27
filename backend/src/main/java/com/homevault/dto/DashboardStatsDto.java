package com.homevault.dto;

import java.util.List;

/** Aggregated stats returned to the family dashboard. */
public class DashboardStatsDto {

    private long totalItems;
    private long expiringItems;   // within 7 days
    private long expiredItems;
    private long totalMembers;
    private long pendingReminders;
    private List<ItemDto> recentlyExpiring;  // top 5 soonest-expiring
    private List<ReminderDto> upcomingReminders; // top 5 upcoming

    public long getTotalItems() { return totalItems; }
    public void setTotalItems(long totalItems) { this.totalItems = totalItems; }

    public long getExpiringItems() { return expiringItems; }
    public void setExpiringItems(long expiringItems) { this.expiringItems = expiringItems; }

    public long getExpiredItems() { return expiredItems; }
    public void setExpiredItems(long expiredItems) { this.expiredItems = expiredItems; }

    public long getTotalMembers() { return totalMembers; }
    public void setTotalMembers(long totalMembers) { this.totalMembers = totalMembers; }

    public long getPendingReminders() { return pendingReminders; }
    public void setPendingReminders(long pendingReminders) { this.pendingReminders = pendingReminders; }

    public List<ItemDto> getRecentlyExpiring() { return recentlyExpiring; }
    public void setRecentlyExpiring(List<ItemDto> recentlyExpiring) { this.recentlyExpiring = recentlyExpiring; }

    public List<ReminderDto> getUpcomingReminders() { return upcomingReminders; }
    public void setUpcomingReminders(List<ReminderDto> upcomingReminders) { this.upcomingReminders = upcomingReminders; }
}
