package com.homevault.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;

public class HouseholdDto {

    private Long id;

    @NotBlank(message = "Household name is required")
    private String name;

    private String description;
    private Long ownerId;
    private String ownerName;
    private List<MemberDto> members;
    private long totalItems;
    private long expiringItems;
    private long pendingReminders;
    private int totalMembers;
    private LocalDateTime createdAt;

    // ── Nested DTO ────────────────────────────────────────────────────────────

    public static class MemberDto {
        private Long id;
        private String name;
        private String email;

        public MemberDto(Long id, String name, String email) {
            this.id = id; this.name = name; this.email = email;
        }
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public List<MemberDto> getMembers() { return members; }
    public void setMembers(List<MemberDto> members) { this.members = members; }

    public long getTotalItems() { return totalItems; }
    public void setTotalItems(long totalItems) { this.totalItems = totalItems; }

    public long getExpiringItems() { return expiringItems; }
    public void setExpiringItems(long expiringItems) { this.expiringItems = expiringItems; }

    public long getPendingReminders() { return pendingReminders; }
    public void setPendingReminders(long pendingReminders) { this.pendingReminders = pendingReminders; }

    public int getTotalMembers() { return totalMembers; }
    public void setTotalMembers(int totalMembers) { this.totalMembers = totalMembers; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
