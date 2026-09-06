package com.example.assistant.service.ai;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CostCalculationService {

    /**
     * Calculates the estimated cost in USD based on model and token counts.
     */
    public BigDecimal calculateCost(String model, int promptTokens, int completionTokens) {
        if (model == null) {
            model = "deepseek-chat";
        }

        double inputCostPerMillion;
        double outputCostPerMillion;

        switch (model.toLowerCase()) {
            case "deepseek-chat", "deepseek-v3" -> {
                inputCostPerMillion = 0.14;
                outputCostPerMillion = 0.28;
            }
            case "deepseek-reasoner", "deepseek-r1" -> {
                inputCostPerMillion = 0.55;
                outputCostPerMillion = 2.19;
            }
            case "gpt-4o" -> {
                inputCostPerMillion = 2.50;
                outputCostPerMillion = 10.00;
            }
            case "gpt-4o-mini" -> {
                inputCostPerMillion = 0.15;
                outputCostPerMillion = 0.60;
            }
            case "gpt-4-turbo", "gpt-4" -> {
                inputCostPerMillion = 10.00;
                outputCostPerMillion = 30.00;
            }
            case "gpt-3.5-turbo" -> {
                inputCostPerMillion = 0.50;
                outputCostPerMillion = 1.50;
            }
            default -> {
                inputCostPerMillion = 0.14;
                outputCostPerMillion = 0.28;
            }
        }

        BigDecimal inputCost = BigDecimal.valueOf(promptTokens)
                .multiply(BigDecimal.valueOf(inputCostPerMillion))
                .divide(BigDecimal.valueOf(1_000_000), 8, RoundingMode.HALF_UP);

        BigDecimal outputCost = BigDecimal.valueOf(completionTokens)
                .multiply(BigDecimal.valueOf(outputCostPerMillion))
                .divide(BigDecimal.valueOf(1_000_000), 8, RoundingMode.HALF_UP);

        return inputCost.add(outputCost).setScale(6, RoundingMode.HALF_UP);
    }
}
