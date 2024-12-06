package com.biengual.core.domain.entity.learning;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryLearningProgressId implements Serializable {
    @Column(name = "user_id", nullable = false, columnDefinition = "bigint")
    private Long userId;

    @Column(name = "category_id", nullable = false, columnDefinition = "bigint")
    private Long categoryId;

    @Builder
    public CategoryLearningProgressId(Long userId, Long categoryId) {
        this.userId = userId;
        this.categoryId = categoryId;
    }

    public static CategoryLearningProgressId create(Long userId, Long categoryId) {
        return CategoryLearningProgressId.builder()
            .userId(userId)
            .categoryId(categoryId)
            .build();
    }
}
