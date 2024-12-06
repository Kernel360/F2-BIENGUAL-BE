package com.biengual.userapi.recommender.domain;

import java.util.List;
import java.util.Set;

/**
 * Recommender 도메인의 DataProvider 계층의 인터페이스
 *
 * @author 김영래
 */
public interface RecommenderReader {
    List<RecommenderInfo.PopularBookmark> findPopularBookmarks();

    RecommenderInfo.PreviewRecommender findContents(Long userId, Set<Long> contentIds);

    Set<Long> findRecommendedContentIdSet(Long userId);
}
