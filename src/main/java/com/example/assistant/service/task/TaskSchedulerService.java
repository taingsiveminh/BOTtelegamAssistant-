package com.example.assistant.service.task;

import com.example.assistant.entity.Task;
import com.example.assistant.repository.TaskRepository;
import com.example.assistant.service.telegram.TelegramMessageSender;
import com.example.assistant.util.MarkdownUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskSchedulerService {

    private final TaskRepository taskRepository;
    private final TaskService taskService;
    private final TelegramMessageSender messageSender;

    @Scheduled(fixedRate = 30000) // Runs every 30 seconds
    public void checkAndDispatchDueReminders() {
        OffsetDateTime now = OffsetDateTime.now();
        List<Task> dueTasks = taskRepository.findDuePendingTasks(now);

        if (dueTasks.isEmpty()) {
            return;
        }

        log.info("Found {} due reminders to dispatch.", dueTasks.size());

        for (Task task : dueTasks) {
            try {
                if (task.getUser() != null && task.getUser().getTelegramUserId() != null) {
                    boolean notifEnabled = task.getUser().getSettings() == null || task.getUser().getSettings().isNotificationsEnabled();

                    if (notifEnabled) {
                        String reminderHtml = """
                                🔔 <b>REMINDER ALERT!</b>
                                
                                📌 <b>Bro, it's time to:</b>
                                %s
                                
                                ⏰ <i>Scheduled for: %s</i>
                                """.formatted(
                                MarkdownUtils.escapeHtml(task.getTitle()),
                                MarkdownUtils.escapeHtml(task.getScheduledAt().toString())
                        );

                        messageSender.sendMessageHtml(task.getUser().getTelegramUserId(), reminderHtml);
                    }
                }

                taskService.markTaskCompleted(task);
                log.info("Dispatched and marked task {} completed for user {}", task.getId(), task.getUser().getTelegramUserId());
            } catch (Exception ex) {
                log.error("Failed to dispatch reminder task {}: {}", task.getId(), ex.getMessage(), ex);
            }
        }
    }
}
