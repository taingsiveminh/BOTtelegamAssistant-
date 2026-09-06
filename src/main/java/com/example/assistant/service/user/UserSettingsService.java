package com.example.assistant.service.user;

import com.example.assistant.entity.BotMode;
import com.example.assistant.entity.User;
import com.example.assistant.entity.UserSettings;
import com.example.assistant.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSettingsService {

    private final UserSettingsRepository userSettingsRepository;

    @Transactional
    public UserSettings getOrCreateSettings(User user) {
        return userSettingsRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserSettings s = UserSettings.builder()
                            .user(user)
                            .language("en")
                            .memoryEnabled(true)
                            .notificationsEnabled(true)
                            .activeMode(BotMode.CHAT)
                            .build();
                    return userSettingsRepository.save(s);
                });
    }

    @Transactional
    public UserSettings updateLanguage(User user, String languageCode) {
        UserSettings settings = getOrCreateSettings(user);
        settings.setLanguage(languageCode);
        return userSettingsRepository.save(settings);
    }

    @Transactional
    public UserSettings toggleMemory(User user) {
        UserSettings settings = getOrCreateSettings(user);
        settings.setMemoryEnabled(!settings.isMemoryEnabled());
        return userSettingsRepository.save(settings);
    }

    @Transactional
    public UserSettings toggleNotifications(User user) {
        UserSettings settings = getOrCreateSettings(user);
        settings.setNotificationsEnabled(!settings.isNotificationsEnabled());
        return userSettingsRepository.save(settings);
    }

    @Transactional
    public UserSettings setActiveMode(User user, BotMode mode) {
        UserSettings settings = getOrCreateSettings(user);
        settings.setActiveMode(mode);
        return userSettingsRepository.save(settings);
    }
}
