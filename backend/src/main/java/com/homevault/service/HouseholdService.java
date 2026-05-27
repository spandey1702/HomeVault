package com.homevault.service;

import com.homevault.dto.HouseholdDto;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HouseholdService {

    @Autowired private HouseholdRepository householdRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private ReminderRepository reminderRepository;

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public HouseholdDto createHousehold(String name, String description, Long ownerId) {
        User owner = requireUser(ownerId);
        Household h = new Household(name, owner);
        h.setDescription(description);
        return toDto(householdRepository.save(h));
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    public List<HouseholdDto> getHouseholdsForUser(Long userId) {
        User user = requireUser(userId);
        return householdRepository.findByMemberId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public HouseholdDto getHousehold(Long householdId, Long requesterId) {
        Household h = requireHousehold(householdId);
        requireMembership(h, requesterId);
        return toDto(h);
    }

    // ── Invite / join ─────────────────────────────────────────────────────────

    @Transactional
    public HouseholdDto inviteMemberByEmail(Long householdId, String email, Long requesterId) {
        Household h = requireHousehold(householdId);
        requireOwnership(h, requesterId);

        User invitee = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No user found with email: " + email));

        boolean alreadyMember = h.getMembers().stream()
                .anyMatch(m -> m.getId().equals(invitee.getId()));
        if (alreadyMember) {
            throw new RuntimeException("User is already a member of this household");
        }

        h.getMembers().add(invitee);
        return toDto(householdRepository.save(h));
    }

    @Transactional
    public void removeMember(Long householdId, Long memberId, Long requesterId) {
        Household h = requireHousehold(householdId);
        requireOwnership(h, requesterId);
        if (h.getOwner().getId().equals(memberId)) {
            throw new RuntimeException("Cannot remove the household owner");
        }
        h.getMembers().removeIf(m -> m.getId().equals(memberId));
        householdRepository.save(h);
    }

    @Transactional
    public void leaveHousehold(Long householdId, Long userId) {
        Household h = requireHousehold(householdId);
        if (h.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Owner cannot leave — transfer ownership or delete the household");
        }
        h.getMembers().removeIf(m -> m.getId().equals(userId));
        householdRepository.save(h);
    }

    @Transactional
    public void deleteHousehold(Long householdId, Long requesterId) {
        Household h = requireHousehold(householdId);
        requireOwnership(h, requesterId);
        householdRepository.delete(h);
    }

    // ── Guards ────────────────────────────────────────────────────────────────

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Household requireHousehold(Long id) {
        return householdRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Household not found"));
    }

    private void requireOwnership(Household h, Long userId) {
        if (!h.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Only the household owner can perform this action");
        }
    }

    private void requireMembership(Household h, Long userId) {
        boolean isMember = h.getMembers().stream().anyMatch(m -> m.getId().equals(userId));
        if (!isMember) {
            throw new RuntimeException("You are not a member of this household");
        }
    }

    // ── DTO conversion ────────────────────────────────────────────────────────

    HouseholdDto toDto(Household h) {
        HouseholdDto dto = new HouseholdDto();
        dto.setId(h.getId());
        dto.setName(h.getName());
        dto.setDescription(h.getDescription());
        dto.setOwnerId(h.getOwner().getId());
        dto.setOwnerName(h.getOwner().getName());
        dto.setCreatedAt(h.getCreatedAt());

        dto.setMembers(h.getMembers().stream()
                .map(m -> new HouseholdDto.MemberDto(m.getId(), m.getName(), m.getEmail()))
                .collect(Collectors.toList()));

        // Live stats
        LocalDate now = LocalDate.now();
        LocalDate in7 = now.plusDays(7);
        long expiring = itemRepository.findExpiringItemsInHousehold(h, now, in7).size();
        long total = itemRepository.countByHousehold(h);
        long pending = reminderRepository.countByHouseholdAndStatus(h, Reminder.Status.PENDING);

        dto.setTotalItems(total);
        dto.setExpiringItems(expiring);
        dto.setPendingReminders(pending);
        dto.setTotalMembers(h.getMembers().size());
        return dto;
    }
}
