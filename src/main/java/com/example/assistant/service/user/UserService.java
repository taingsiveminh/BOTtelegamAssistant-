package com.example.assistant.service.user;

import com.example.assistant.dto.telegram.TelegramUser;
import com.example.assistant.entity.User;
import com.example.assistant.entity.UserSettings;
import com.example.assistant.entity.UserStatus;
import com.example.assistant.entity.UserTier;
import com.example.assistant.repository.UserRepository;
import com.example.assistant.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;

    @Transactional
    public User registerOrUpdateUser(TelegramUser tgUser) {
        if (tgUser == null || tgUser.getId() == null) {
            throw new IllegalArgumentException("Telegram user info is missing");
        }

        Optional<User> existingUserOpt = userRepository.findByTelegramUserId(tgUser.getId());

        if (existingUserOpt.isPresent()) {
            User existing = existingUserOpt.get();
            existing.setUsername(tgUser.getUsername());
            existing.setFirstName(tgUser.getFirstName());
            existing.setLastName(tgUser.getLastName());
            return userRepository.save(existing);
        }

        String initialLang = tgUser.getLanguageCode() != null ? tgUser.getLanguageCode() : "en";

        User newUser = User.builder()
                .telegramUserId(tgUser.getId())
                .username(tgUser.getUsername())
                .firstName(tgUser.getFirstName())
                .lastName(tgUser.getLastName())
                .language(initialLang)
                .tier(UserTier.FREE)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(newUser);

        UserSettings settings = UserSettings.builder()
                .user(savedUser)
                .language(initialLang)
                .memoryEnabled(true)
                .notificationsEnabled(true)
                .build();

        userSettingsRepository.save(settings);
        savedUser.setSettings(settings);

        log.info("Registered new Telegram user: {} (@{})", savedUser.getTelegramUserId(), savedUser.getUsername());
        return savedUser;
    }

    public Optional<User> findByTelegramId(Long telegramId) {
        return userRepository.findByTelegramUserId(telegramId);
    }
}
