package com.example.assistant.service.user;

import com.example.assistant.dto.telegram.TelegramUser;
import com.example.assistant.entity.User;
import com.example.assistant.entity.UserStatus;
import com.example.assistant.entity.UserTier;
import com.example.assistant.repository.UserRepository;
import com.example.assistant.repository.UserSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSettingsRepository userSettingsRepository;

    @InjectMocks
    private UserService userService;

    private TelegramUser tgUser;

    @BeforeEach
    void setUp() {
        tgUser = new TelegramUser();
        tgUser.setId(123456789L);
        tgUser.setUsername("testuser");
        tgUser.setFirstName("John");
        tgUser.setLastName("Doe");
        tgUser.setLanguageCode("en");
    }

    @Test
    void testRegisterNewUser() {
        when(userRepository.findByTelegramUserId(123456789L)).thenReturn(Optional.empty());

        User created = User.builder()
                .id(1L)
                .telegramUserId(123456789L)
                .username("testuser")
                .firstName("John")
                .lastName("Doe")
                .language("en")
                .tier(UserTier.FREE)
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(created);

        User result = userService.registerOrUpdateUser(tgUser);

        assertNotNull(result);
        assertEquals(123456789L, result.getTelegramUserId());
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userSettingsRepository, times(1)).save(any());
    }

    @Test
    void testUpdateExistingUser() {
        User existing = User.builder()
                .id(1L)
                .telegramUserId(123456789L)
                .username("olduser")
                .firstName("Old")
                .build();

        when(userRepository.findByTelegramUserId(123456789L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenReturn(existing);

        User result = userService.registerOrUpdateUser(tgUser);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).save(existing);
        verify(userSettingsRepository, never()).save(any());
    }
}
