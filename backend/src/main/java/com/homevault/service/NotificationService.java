package com.homevault.service;

import com.homevault.entity.Item;
import com.homevault.entity.Reminder;
import com.homevault.repository.ItemRepository;
import com.homevault.repository.ReminderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Scheduled service that:
 * 1. Every morning at 08:00 — emails household members about items expiring in ≤7 days.
 * 2. Every morning at 08:00 — emails reminders that are overdue or due today.
 *
 * Set MAIL_ENABLED=false in your .env to suppress sending during local dev.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Autowired private ItemRepository itemRepository;
    @Autowired private ReminderRepository reminderRepository;
    @Autowired(required = false) private JavaMailSender mailSender;

    @Value("${app.notifications.enabled:false}")
    private boolean notificationsEnabled;

    @Value("${app.notifications.from:noreply@homevault.app}")
    private String fromAddress;

    // ── Expiry check — runs every day at 08:00 server time ───────────────────

    @Scheduled(cron = "0 0 8 * * *")
    public void sendExpiryAlerts() {
        if (!notificationsEnabled || mailSender == null) {
            log.info("Expiry notifications disabled — skipping.");
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate in7   = today.plusDays(7);

        List<Item> expiring = itemRepository.findAllExpiringItems(today, in7);

        // Group by household owner's email so one email per user
        Map<String, List<Item>> byEmail = expiring.stream()
                .filter(i -> i.getOwner() != null)
                .collect(Collectors.groupingBy(i -> i.getOwner().getEmail()));

        byEmail.forEach((email, items) -> {
            String subject = "⚠️ HomeVault: " + items.size() + " item(s) expiring soon";
            String body = buildExpiryBody(items);
            sendEmail(email, subject, body);
        });

        log.info("Expiry alerts sent for {} users.", byEmail.size());
    }

    // ── Reminder check — runs every day at 08:00 server time ─────────────────

    @Scheduled(cron = "0 0 8 * * *")
    public void sendReminderAlerts() {
        if (!notificationsEnabled || mailSender == null) {
            log.info("Reminder notifications disabled — skipping.");
            return;
        }

        List<Reminder> overdue = reminderRepository.findOverdueReminders(LocalDate.now());

        Map<String, List<Reminder>> byEmail = overdue.stream()
                .collect(Collectors.groupingBy(r -> r.getCreatedBy().getEmail()));

        byEmail.forEach((email, reminders) -> {
            String subject = "🔔 HomeVault: " + reminders.size() + " reminder(s) need your attention";
            String body = buildReminderBody(reminders);
            sendEmail(email, subject, body);
        });

        log.info("Reminder alerts sent for {} users.", byEmail.size());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String buildExpiryBody(List<Item> items) {
        StringBuilder sb = new StringBuilder("The following items in your HomeVault are expiring soon:\n\n");
        items.forEach(i -> sb.append(String.format(
                "• %s  (expires %s)%s\n",
                i.getName(),
                i.getExpiryDate(),
                i.getHousehold() != null ? "  [" + i.getHousehold().getName() + "]" : ""
        )));
        sb.append("\nLog in to HomeVault to manage your inventory.");
        return sb.toString();
    }

    private String buildReminderBody(List<Reminder> reminders) {
        StringBuilder sb = new StringBuilder("You have overdue or due-today reminders:\n\n");
        reminders.forEach(r -> sb.append(String.format(
                "• %s  (due %s)%s\n",
                r.getTitle(),
                r.getDueDate() != null ? r.getDueDate() : "no date",
                r.getHousehold() != null ? "  [" + r.getHousehold().getName() + "]" : ""
        )));
        sb.append("\nLog in to HomeVault to review your reminders.");
        return sb.toString();
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromAddress);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
            log.info("Email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
