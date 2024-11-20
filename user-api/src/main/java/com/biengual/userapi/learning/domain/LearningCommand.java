package com.biengual.userapi.learning.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.biengual.core.domain.entity.learning.CategoryLearningHistoryEntity;
import com.biengual.core.domain.entity.learning.LearningHistoryEntity;
import com.biengual.core.domain.entity.learning.RecentLearningHistoryEntity;

import lombok.Builder;

public class LearningCommand {

    @Builder
    public record RecordLearningRate(
        Long userId,
        Long contentId,
        BigDecimal learningRate,
        LocalDateTime learningTime
    ) {
        public RecentLearningHistoryEntity toUserLearningHistoryEntity() {
            return RecentLearningHistoryEntity.builder()
                .userId(this.userId)
                .contentId(this.contentId)
                .currentLearningRate(this.learningRate)
                .completedLearningRate(this.learningRate)
                .recentLearningTime(this.learningTime)
                .build();
        }

        public LearningHistoryEntity toLearningHistoryEntity() {
            return LearningHistoryEntity.builder()
                .userId(this.userId)
                .contentId(this.contentId)
                .learningRate(this.learningRate)
                .learningTime(this.learningTime)
                .build();
        }

        public CategoryLearningHistoryEntity toCategoryLearningHistoryEntity(Long categoryId) {
            return CategoryLearningHistoryEntity.builder()
                .userId(this.userId)
                .contentId(this.contentId)
                .categoryId(categoryId)
                .learningTime(this.learningTime)
                .build();
        }
    }
}
