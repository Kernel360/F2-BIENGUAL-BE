package com.biengual.core.domain.entity.learning;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import static com.biengual.core.constant.RestrictionConstant.LEARNING_COMPLETION_RATE_THRESHOLD;

@Getter
@Entity
@Table(name = "category_learning_progress")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryLearningProgressEntity {
    @EmbeddedId
    private CategoryLearningProgressId categoryLearningProgressId;

    @Column(nullable = false, columnDefinition = "bigint")
    private Long totalLearningCount;

    // TODO: RecentLearningHistoryEntity의 completedLearning 과는 의미가 다른 completedLearning 이라
    //  RecentLearningHistoryEntity의 completedLearning 네이밍을 변경해야 헷갈리지 않을 것 같습니다.
    // 학습 완료 기준을 충족하는 학습량
    @Column(nullable = false, columnDefinition = "bigint")
    private Long completedLearningCount;

    @Builder
    public CategoryLearningProgressEntity(
        CategoryLearningProgressId categoryLearningProgressId, Long totalLearningCount, Long completedLearningCount
    ) {
        this.categoryLearningProgressId = categoryLearningProgressId;
        this.totalLearningCount = totalLearningCount;
        this.completedLearningCount = completedLearningCount;
    }

    // 학습 상황 업데이트
    public void updateProgress(BigDecimal learningRate) {
        this.totalLearningCount += 1;
        this.completedLearningCount =
            learningRate.compareTo(BigDecimal.valueOf(LEARNING_COMPLETION_RATE_THRESHOLD)) >= 0
            ? this.completedLearningCount + 1
            : this.completedLearningCount;
    }
}
