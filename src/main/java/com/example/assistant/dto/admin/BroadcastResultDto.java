package com.example.assistant.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BroadcastResultDto {
    private int totalTargeted;
    private int successfullySent;
    private int failed;
    private String status;
}
