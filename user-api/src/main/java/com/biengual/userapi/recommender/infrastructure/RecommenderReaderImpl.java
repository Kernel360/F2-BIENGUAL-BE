package com.biengual.userapi.recommender.infrastructure;

import com.biengual.core.annotation.DataProvider;
import com.biengual.core.annotation.RedisCacheable;
import com.biengual.core.util.PeriodUtil;
import com.biengual.userapi.content.domain.ContentCustomRepository;
import com.biengual.userapi.recommender.application.ContentRecommender;
import com.biengual.userapi.recommender.domain.RecommenderCustomRepository;
import com.biengual.userapi.recommender.domain.RecommenderInfo;
import com.biengual.userapi.recommender.domain.RecommenderReader;
import com.biengual.userapi.validator.RecommenderValidator;
import lombok.RequiredArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.biengual.core.constant.RedisUniqueKeyConstant.CONTENT_RECOMMENDATION;
import static com.biengual.core.constant.RestrictionConstant.MAX_RECOMMENDED_CONTENT_COUNT;

@DataProvider
@RequiredArgsConstructor
public class RecommenderReaderImpl implements RecommenderReader {
    private final RecommenderCustomRepository recommenderCustomRepository;
    private final ContentCustomRepository contentCustomRepository;
    private final ContentRecommender contentRecommender;
    private final RecommenderValidator recommenderValidator;

    @Override
    public List<RecommenderInfo.PopularBookmark> findPopularBookmarks() {
        LocalDate lastWeek =
            PeriodUtil.getFewWeeksAgo(LocalDate.from(LocalDateTime.now()), 1, DayOfWeek.MONDAY);

        LocalDateTime startOfWeek = PeriodUtil.getStartOfWeek(lastWeek);
        LocalDateTime endOfWeek = PeriodUtil.getEndOfWeek(lastWeek);

        return recommenderCustomRepository.findPopularBookmarks(startOfWeek, endOfWeek);
    }

    @Override
    public RecommenderInfo.PreviewRecommender findContents(Long userId, Set<Long> contentIds) {
        return RecommenderInfo.PreviewRecommender
            .of(contentCustomRepository.findRecommendedContentsIn(userId, contentIds));
    }

    @Override
    @RedisCacheable(key = "#userId", value = CONTENT_RECOMMENDATION, ttl = 10, timeUnit = TimeUnit.MINUTES)
    public Set<Long> findRecommendedContentIdSet(Long userId) {
        // 첫 번째 추천
        Set<Long> recommendedContentIdSet =
            new HashSet<>(contentRecommender.recommendBasedOnSimilarUsers(userId, MAX_RECOMMENDED_CONTENT_COUNT));

        // 두 번째 추천
        if (!recommenderValidator.verifyRecommendedContentCount(recommendedContentIdSet)) {
            int requiredContentCount = MAX_RECOMMENDED_CONTENT_COUNT - recommendedContentIdSet.size();

            recommendedContentIdSet
                .addAll(contentRecommender.recommendBasedOnUserCategory(userId, requiredContentCount));
        }

        // 세 번째 추천
        if (!recommenderValidator.verifyRecommendedContentCount(recommendedContentIdSet)) {
            int requiredContentCount = MAX_RECOMMENDED_CONTENT_COUNT - recommendedContentIdSet.size();

            recommendedContentIdSet
                .addAll(contentRecommender.recommendBasedOnPopularity(requiredContentCount));
        }

        return recommendedContentIdSet;
    }
}
