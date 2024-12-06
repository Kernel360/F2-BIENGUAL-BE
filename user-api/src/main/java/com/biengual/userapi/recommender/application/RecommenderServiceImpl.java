package com.biengual.userapi.recommender.application;

import com.biengual.userapi.recommender.domain.RecommenderInfo;
import com.biengual.userapi.recommender.domain.RecommenderReader;
import com.biengual.userapi.recommender.domain.RecommenderService;
import com.biengual.userapi.recommender.domain.RecommenderStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RecommenderServiceImpl implements RecommenderService {
    private final RecommenderStore recommenderStore;
    private final RecommenderReader recommenderReader;

    // 카테고리 기반 컨텐츠 추천
    @Override
    @Transactional(readOnly = true)
    public RecommenderInfo.PreviewRecommender getRecommendedContentsByCategory(Long userId) {
        Set<Long> recommendedContentIdSet = recommenderReader.findRecommendedContentIdSet(userId);

        return recommenderReader.findContents(userId, recommendedContentIdSet);
    }

    @Override
    @Transactional(readOnly = true)
    public RecommenderInfo.PopularBookmarkRecommender getPopularBookmarks() {
        return RecommenderInfo.PopularBookmarkRecommender.of(recommenderReader.findPopularBookmarks());
    }

    @Override
    @Transactional
    public void updatePopularBookmarks() {
        recommenderStore.createLastWeekBookmarkRecommender();
    }
}
