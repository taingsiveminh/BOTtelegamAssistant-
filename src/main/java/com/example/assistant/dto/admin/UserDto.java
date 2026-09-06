package com.example.assistant.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private Long telegramUserId;
    private String username;
    private String firstName;
    private String lastName;
    private String language;
    private String tier;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
