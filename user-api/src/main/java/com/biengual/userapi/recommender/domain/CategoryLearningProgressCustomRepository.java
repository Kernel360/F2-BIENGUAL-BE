package com.biengual.userapi.recommender.domain;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.biengual.core.domain.entity.learning.QCategoryLearningProgressEntity.categoryLearningProgressEntity;

@Repository
@RequiredArgsConstructor
public class CategoryLearningProgressCustomRepository {
    private final JPAQueryFactory queryFactory;

    public boolean existsByUserId(Long userId) {
        return queryFactory
            .select(categoryLearningProgressEntity.categoryLearningProgressId.userId)
            .from(categoryLearningProgressEntity)
            .where(categoryLearningProgressEntity.categoryLearningProgressId.userId.eq(userId))
            .fetchFirst() != null;
    }
}
