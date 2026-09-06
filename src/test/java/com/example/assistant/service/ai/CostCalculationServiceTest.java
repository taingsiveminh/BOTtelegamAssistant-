package com.example.assistant.service.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CostCalculationServiceTest {

    private CostCalculationService costCalculationService;

    @BeforeEach
    void setUp() {
        costCalculationService = new CostCalculationService();
    }

    @Test
    void testCalculateCostGpt4oMini() {
        BigDecimal cost = costCalculationService.calculateCost("gpt-4o-mini", 1000, 500);
        assertNotNull(cost);
        assertTrue(cost.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testCalculateCostGpt4o() {
        BigDecimal cost = costCalculationService.calculateCost("gpt-4o", 1000, 500);
        assertNotNull(cost);
        assertTrue(cost.compareTo(BigDecimal.ZERO) > 0);
    }
}
