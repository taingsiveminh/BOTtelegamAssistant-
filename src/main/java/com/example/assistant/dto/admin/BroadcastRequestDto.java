package com.example.assistant.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BroadcastRequestDto {

    @NotBlank(message = "Broadcast message cannot be blank")
    private String message;

    private String targetTier; // "ALL", "FREE", "PREMIUM"
}
