package com.biengual.userapi.learning.domain;

import com.biengual.core.enums.ContentStatus;
import com.biengual.userapi.content.domain.ContentInfo;
import com.biengual.userapi.dashboard.domain.DashboardInfo;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.biengual.core.constant.RestrictionConstant.LEARNING_COMPLETION_RATE_THRESHOLD;
import static com.biengual.core.domain.entity.content.QContentEntity.contentEntity;
import static com.biengual.core.domain.entity.learning.QRecentLearningHistoryEntity.recentLearningHistoryEntity;
import static com.biengual.core.domain.entity.scrap.QScrapEntity.scrapEntity;

@Repository
@RequiredArgsConstructor
public class RecentLearningHistoryCustomRepository {
    private final JPAQueryFactory queryFactory;

    // 해당 컨텐츠의 유저 현재 학습률을 조회하기 위한 쿼리
    public Optional<ContentInfo.LearningRateInfo> findLearningRateByUserIdAndContentId(Long userId, Long contentId) {
        return Optional.ofNullable(
            queryFactory
                .select(
                    Projections.constructor(
                        ContentInfo.LearningRateInfo.class,
                        recentLearningHistoryEntity.currentLearningRate,
                        recentLearningHistoryEntity.completedLearningRate
                    )
                )
                .from(recentLearningHistoryEntity)
                .where(recentLearningHistoryEntity.userId.eq(userId)
                    .and(recentLearningHistoryEntity.contentId.eq(contentId)))
                .fetchOne()
        );
    }

    // 최근 학습 컨텐츠 1개를 요약하여 조회하기 위한 쿼리
    public DashboardInfo.RecentLearningSummary findRecentLearningSummaryByUserId(Long userId) {
        return queryFactory
            .select(
                Projections.constructor(
                    DashboardInfo.RecentLearningSummary.class,
                    contentEntity.title,
                    recentLearningHistoryEntity.completedLearningRate
                )
            )
            .from(recentLearningHistoryEntity)
            .innerJoin(contentEntity)
            .on(recentLearningHistoryEntity.contentId.eq(contentEntity.id))
            .where(recentLearningHistoryEntity.userId.eq(userId))
            .orderBy(recentLearningHistoryEntity.recentLearningTime.desc())
            .fetchFirst();
    }

    // TODO: 8개로 고정할 것인지?
    // 최근 학습 컨텐츠 8개를 학습률과 함께 조회하기 위한 쿼리
    public List<DashboardInfo.RecentLearning> findRecentLearningTop8ByUserId(Long userId) {
        return queryFactory
            .select(
                Projections.constructor(
                    DashboardInfo.RecentLearning.class,
                    contentEntity.id,
                    contentEntity.title,
                    contentEntity.s3Url,
                    contentEntity.contentType,
                    contentEntity.preScripts,
                    contentEntity.category.name,
                    contentEntity.videoDuration,
                    contentEntity.hits,
                    contentEntity.contentLevel,
                    getIsScrappedByUserId(userId),
                    recentLearningHistoryEntity.currentLearningRate,
                    recentLearningHistoryEntity.completedLearningRate
                )
            )
            .from(recentLearningHistoryEntity)
            .innerJoin(contentEntity)
            .on(recentLearningHistoryEntity.contentId.eq(contentEntity.id))
            .where(recentLearningHistoryEntity.userId.eq(userId))
            .limit(8)
            .orderBy(recentLearningHistoryEntity.recentLearningTime.desc())
            .fetch();
    }

    // 자신이 학습 완료했던 Content Id를 조회하는 쿼리
    public List<Long> findLearningCompletionContentIdsByUserId(Long userId) {
        return queryFactory
            .select(recentLearningHistoryEntity.contentId)
            .from(recentLearningHistoryEntity)
            .where(
                recentLearningHistoryEntity.userId.eq(userId)
                    .and(recentLearningHistoryEntity.completedLearningRate.goe(LEARNING_COMPLETION_RATE_THRESHOLD))
            )
            .fetch();
    }

    // 추천할 Content Id를 최대 limit만큼 조회하는 쿼리
    public List<Long> findRecommendedContentIdsWithLimit(List<Long> userIds, List<Long> contentIds, int limit) {
        return queryFactory
            .select(recentLearningHistoryEntity.contentId)
            .from(recentLearningHistoryEntity)
            .join(contentEntity)
            .on(recentLearningHistoryEntity.contentId.eq(contentEntity.id))
            .where(
                recentLearningHistoryEntity.userId.in(userIds)
                    .and(recentLearningHistoryEntity.contentId.notIn(contentIds))
                    .and(contentEntity.contentStatus.eq(ContentStatus.ACTIVATED))
            )
            .orderBy(recentLearningHistoryEntity.completedLearningRate.desc(),
                recentLearningHistoryEntity.currentLearningRate.desc())
            .limit(limit)
            .fetch();
    }

    // Internal Methods ================================================================================================
    // isScrapped를 col로 받기 위한 확인하는 쿼리, 비로그인 상태 시 false 리턴
    private Expression<?> getIsScrappedByUserId(Long userId) {
        return userId != null ?
            JPAExpressions
                .selectOne()
                .from(scrapEntity)
                .where(scrapEntity.content.id.eq(contentEntity.id)
                    .and(scrapEntity.userId.eq(userId)))
                .exists()
            : Expressions.constant(false);
    }
}
