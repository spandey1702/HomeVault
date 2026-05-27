package com.homevault.service;

import com.homevault.dto.ReminderDto;
import com.homevault.entity.Reminder;
import com.homevault.entity.User;
import com.homevault.repository.HouseholdRepository;
import com.homevault.repository.ReminderRepository;
import com.homevault.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReminderService {

    @Autowired private ReminderRepository reminderRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private HouseholdRepository householdRepository;

    public List<ReminderDto> getRemindersForUser(Long userId) {
        User user = requireUser(userId);
        return reminderRepository.findAllForUser(user).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public ReminderDto createReminder(ReminderDto dto, Long userId) {
        User user = requireUser(userId);
        Reminder r = new Reminder();
        r.setTitle(dto.getTitle());
        r.setDescription(dto.getDescription());
        r.setDueDate(dto.getDueDate());
        r.setStatus(Reminder.Status.PENDING);
        r.setCreatedBy(user);

        if (dto.getHouseholdId() != null) {
            householdRepository.findById(dto.getHouseholdId())
                    .ifPresent(r::setHousehold);
        }
        return toDto(reminderRepository.save(r));
    }

    @Transactional
    public ReminderDto updateReminder(Long reminderId, ReminderDto dto, Long userId) {
        Reminder r = requireReminder(reminderId);
        requireOwnership(r, userId);

        if (dto.getTitle()       != null) r.setTitle(dto.getTitle());
        if (dto.getDescription() != null) r.setDescription(dto.getDescription());
        if (dto.getDueDate()     != null) r.setDueDate(dto.getDueDate());
        if (dto.getStatus()      != null) r.setStatus(dto.getStatus());
        return toDto(reminderRepository.save(r));
    }

    @Transactional
    public ReminderDto markDone(Long reminderId, Long userId) {
        Reminder r = requireReminder(reminderId);
        requireOwnership(r, userId);
        r.setStatus(Reminder.Status.DONE);
        return toDto(reminderRepository.save(r));
    }

    @Transactional
    public void deleteReminder(Long reminderId, Long userId) {
        Reminder r = requireReminder(reminderId);
        requireOwnership(r, userId);
        reminderRepository.delete(r);
    }

    // ── Guards ────────────────────────────────────────────────────────────────

    private User requireUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Reminder requireReminder(Long id) {
        return reminderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reminder not found"));
    }

    private void requireOwnership(Reminder r, Long userId) {
        if (!r.getCreatedBy().getId().equals(userId)) {
            throw new RuntimeException("Not authorised to modify this reminder");
        }
    }

    // ── DTO conversion ────────────────────────────────────────────────────────

    ReminderDto toDto(Reminder r) {
        ReminderDto dto = new ReminderDto();
        dto.setId(r.getId());
        dto.setTitle(r.getTitle());
        dto.setDescription(r.getDescription());
        dto.setDueDate(r.getDueDate());
        dto.setStatus(r.getStatus());
        dto.setCreatedById(r.getCreatedBy().getId());
        dto.setCreatedByName(r.getCreatedBy().getName());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());
        if (r.getHousehold() != null) {
            dto.setHouseholdId(r.getHousehold().getId());
            dto.setHouseholdName(r.getHousehold().getName());
        }
        return dto;
    }
}
