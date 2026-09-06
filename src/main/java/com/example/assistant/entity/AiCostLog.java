package com.example.assistant.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ai_cost_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiCostLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Column(name = "prompt_tokens", nullable = false)
    @Builder.Default
    private int promptTokens = 0;

    @Column(name = "completion_tokens", nullable = false)
    @Builder.Default
    private int completionTokens = 0;

    @Column(name = "total_tokens", nullable = false)
    @Builder.Default
    private int totalTokens = 0;

    @Column(name = "estimated_cost", nullable = false, precision = 10, scale = 6)
    @Builder.Default
    private BigDecimal estimatedCost = BigDecimal.ZERO;

    @Column(name = "latency_ms", nullable = false)
    @Builder.Default
    private long latencyMs = 0L;

    @Column(name = "feature", nullable = false, length = 50)
    @Builder.Default
    private String feature = "CHAT";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
