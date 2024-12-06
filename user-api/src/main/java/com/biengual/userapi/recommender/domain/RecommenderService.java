package com.biengual.userapi.recommender.domain;

public interface RecommenderService {
    RecommenderInfo.PreviewRecommender getRecommendedContentsByCategory(Long userId);

    RecommenderInfo.PopularBookmarkRecommender getPopularBookmarks();

    void updatePopularBookmarks();
}
