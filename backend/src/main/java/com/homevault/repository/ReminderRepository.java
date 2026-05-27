package com.homevault.repository;

import com.homevault.entity.Household;
import com.homevault.entity.Reminder;
import com.homevault.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    /** All reminders visible to a user (their own + their household's). */
    @Query("SELECT r FROM Reminder r WHERE r.createdBy = :user " +
           "OR r.household IN (SELECT h FROM Household h JOIN h.members m WHERE m = :user) " +
           "ORDER BY r.dueDate ASC NULLS LAST, r.createdAt DESC")
    List<Reminder> findAllForUser(@Param("user") User user);

    List<Reminder> findByHousehold(Household household);

    /** Reminders due today or earlier that are still PENDING. */
    @Query("SELECT r FROM Reminder r WHERE r.status = 'PENDING' " +
           "AND r.dueDate IS NOT NULL AND r.dueDate <= :today " +
           "ORDER BY r.dueDate ASC")
    List<Reminder> findOverdueReminders(@Param("today") LocalDate today);

    long countByHouseholdAndStatus(Household household, Reminder.Status status);
}
