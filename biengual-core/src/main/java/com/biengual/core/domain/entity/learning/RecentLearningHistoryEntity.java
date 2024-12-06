package com.biengual.core.domain.entity.learning;

import com.biengual.core.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "recent_learning_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecentLearningHistoryEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "bigint")
    private Long userId;

    @Column(nullable = false, columnDefinition = "bigint")
    private Long contentId;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal currentLearningRate;

    // 해당 컨텐츠에 대하여 최대로 학습한 학습률
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal completedLearningRate;

    @Column(nullable = false)
    private LocalDateTime recentLearningTime;

    @Builder
    public RecentLearningHistoryEntity(
        Long id, Long userId, Long contentId,
        BigDecimal currentLearningRate, BigDecimal completedLearningRate, LocalDateTime recentLearningTime
    ) {
        this.id = id;
        this.userId = userId;
        this.contentId = contentId;
        this.currentLearningRate = currentLearningRate;
        this.completedLearningRate = completedLearningRate;
        this.recentLearningTime = recentLearningTime;
    }

    public void record(BigDecimal learningRate, LocalDateTime recentLearningTime) {
        this.currentLearningRate = learningRate;
        this.completedLearningRate = this.completedLearningRate.compareTo(learningRate) < 0
            ? learningRate : this.completedLearningRate;
        this.recentLearningTime = recentLearningTime;
    }
}
