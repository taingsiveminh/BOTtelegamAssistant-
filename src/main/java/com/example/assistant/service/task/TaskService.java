package com.example.assistant.service.task;

import com.example.assistant.dto.task.TaskCreateDto;
import com.example.assistant.dto.task.TaskDto;
import com.example.assistant.entity.Task;
import com.example.assistant.entity.TaskStatus;
import com.example.assistant.entity.User;
import com.example.assistant.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskParserService taskParserService;

    @Transactional
    public Task createTaskFromNaturalLanguage(User user, String text) {
        TaskParserService.ParsedTask parsed = taskParserService.parse(text);

        Task task = Task.builder()
                .user(user)
                .title(parsed.getTitle())
                .description(text)
                .scheduledAt(parsed.getScheduledAt())
                .status(TaskStatus.PENDING)
                .build();

        return taskRepository.save(task);
    }

    @Transactional
    public Task createTask(User user, TaskCreateDto dto) {
        Task task = Task.builder()
                .user(user)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .scheduledAt(dto.getScheduledAt())
                .status(TaskStatus.PENDING)
                .build();
        return taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public List<TaskDto> getUserPendingTasks(User user) {
        return taskRepository.findAllByUserIdAndStatusOrderByScheduledAtAsc(user.getId(), TaskStatus.PENDING)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean deleteTask(User user, Long taskId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent() && taskOpt.get().getUser().getId().equals(user.getId())) {
            Task task = taskOpt.get();
            task.setStatus(TaskStatus.CANCELLED);
            taskRepository.save(task);
            return true;
        }
        return false;
    }

    @Transactional
    public void markTaskCompleted(Task task) {
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(OffsetDateTime.now());
        taskRepository.save(task);
    }

    private TaskDto mapToDto(Task task) {
        return TaskDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .scheduledAt(task.getScheduledAt())
                .status(task.getStatus().name())
                .createdAt(task.getCreatedAt())
                .build();
    }
}
