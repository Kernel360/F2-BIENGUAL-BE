package com.biengual.userapi.learning.domain;

import static com.biengual.core.domain.entity.learning.QLearningHistoryEntity.*;

import java.time.LocalDateTime;
import java.time.YearMonth;

import org.springframework.stereotype.Repository;

import com.biengual.core.util.PeriodUtil;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class LearningHistoryCustomRepository {
    private final JPAQueryFactory queryFactory;

    public boolean existsByUserIdAndContentIdInMonth(Long userId, Long contentId, YearMonth yearMonth) {
        LocalDateTime startOfMonth = PeriodUtil.getStartOfMonth(yearMonth);
        LocalDateTime endOfMonth = PeriodUtil.getEndOfMonth(yearMonth);

        return queryFactory
            .select(learningHistoryEntity.id)
            .from(learningHistoryEntity)
            .where(
                learningHistoryEntity.userId.eq(userId)
                    .and(learningHistoryEntity.contentId.eq(contentId))
                    .and(learningHistoryEntity.learningTime.between(startOfMonth, endOfMonth))
            )
            .fetchFirst() != null;
    }
}
