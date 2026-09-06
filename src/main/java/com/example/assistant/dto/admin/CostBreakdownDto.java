package com.example.assistant.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostBreakdownDto {
    private String model;
    private long requestCount;
    private long totalTokens;
    private BigDecimal estimatedCost;
}
