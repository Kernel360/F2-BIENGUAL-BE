package com.biengual.userapi.recommender.application;

import com.biengual.core.domain.entity.learning.CategoryLearningProgressEntity;
import com.biengual.userapi.category.domain.CategoryRepository;
import com.biengual.userapi.content.domain.ContentCustomRepository;
import com.biengual.userapi.learning.domain.RecentLearningHistoryCustomRepository;
import com.biengual.userapi.recommender.domain.CategoryLearningProgressRepository;
import com.biengual.userapi.user.domain.UserCategoryCustomRepository;
import com.biengual.userapi.validator.RecommenderValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.biengual.core.constant.RestrictionConstant.SIMILAR_USER_THRESHOLD_FOR_FIRST_CONTENT_RECOMMENDATION;

@Component
@RequiredArgsConstructor
public class ContentRecommender {
    private final CategoryRepository categoryRepository;
    private final CategoryLearningProgressRepository categoryLearningProgressRepository;
    private final RecentLearningHistoryCustomRepository recentLearningHistoryCustomRepository;
    private final UserCategoryCustomRepository userCategoryCustomRepository;
    private final ContentCustomRepository contentCustomRepository;
    private final RecommenderValidator recommenderValidator;

    // 첫 번째 컨텐츠 추천
    public List<Long> recommendBasedOnSimilarUsers(Long userId, int limit) {
        if (!recommenderValidator.verifyFirstContentRecommendationCondition(userId)) {
            return Collections.emptyList();
        }

        long categoryCount = categoryRepository.count();

        Map<Long, Long[]> vectorMap = this.calculateContentRecommenderVector((int) (categoryCount + 1));
        Long[] targetUserVector = vectorMap.get(userId);

        Map<Long, Double> similarityMap = this.calculateSimilarityMap(userId, vectorMap, targetUserVector);

        List<Long> similarUserList = this.findMostSimilarUserList(similarityMap);

        List<Long> learningCompletionContentIdList =
            recentLearningHistoryCustomRepository.findLearningCompletionContentIdsByUserId(userId);

        return recentLearningHistoryCustomRepository
            .findRecommendedContentIdsWithLimit(similarUserList, learningCompletionContentIdList, limit);
    }

    // 두 번째 컨텐츠 추천
    public List<Long> recommendBasedOnUserCategory(Long userId, int limit) {
        List<Long> targetUserCategoryIdList = userCategoryCustomRepository.findAllMyRegisteredCategoryId(userId);

        if (!recommenderValidator.verifySecondContentRecommendationCondition(targetUserCategoryIdList)) {
            return Collections.emptyList();
        }

        return contentCustomRepository
            .findPopularContentIdsInCategoryIdsWithLimit(targetUserCategoryIdList, limit);
    }

    // 세 번째 컨텐츠 추천
    public List<Long> recommendBasedOnPopularity(int limit) {
        return contentCustomRepository.findPopularContentIdsWithLimit(limit);
    }

    // Internal Method =================================================================================================

    // 컨텐츠 추천의 코사인 유사도에 사용할 벡터 계산
    private Map<Long, Long[]> calculateContentRecommenderVector(int vectorSize) {
        List<CategoryLearningProgressEntity> categoryLearningProgressList = categoryLearningProgressRepository.findAll();

        Map<Long, Long[]> vectorMap = new HashMap<>();

        for (CategoryLearningProgressEntity categoryLearningProgress : categoryLearningProgressList) {
            Long userId = categoryLearningProgress.getCategoryLearningProgressId().getUserId();
            Long categoryId = categoryLearningProgress.getCategoryLearningProgressId().getCategoryId();
            Long totalLearningCount = categoryLearningProgress.getTotalLearningCount();
            Long completedLearningCount = categoryLearningProgress.getCompletedLearningCount();

            Long[] vector = vectorMap.computeIfAbsent(userId, k -> this.initializeVector(vectorSize));

            this.updateVector(vector, categoryId, totalLearningCount, completedLearningCount, vectorSize);
        }

        return vectorMap;
    }

    // 초기화된 벡터 생성
    private Long[] initializeVector(int vectorSize) {
        Long[] vector = new Long[vectorSize];
        Arrays.fill(vector, 0L); // 기본값 0으로 초기화
        return vector;
    }

    // 벡터 업데이트
    private void updateVector(
        Long[] vector, Long categoryId, Long totalLearningCount, Long completedLearningCount, int vectorSize
    ) {
        int index = Math.toIntExact(categoryId);
        vector[index] = totalLearningCount;
        vector[vectorSize - 1] += completedLearningCount;
    }

    // 타겟 유저의 다른 모든 유저에 대한 유사도 Map 계산
    private Map<Long, Double> calculateSimilarityMap(Long userId, Map<Long, Long[]> vectorMap, Long[] targetUserVector) {
        Map<Long, Double> similarityMap = new HashMap<>();

        for (Map.Entry<Long, Long[]> entry : vectorMap.entrySet()) {

            if (Objects.equals(entry.getKey(), userId)) continue;

            double similarity = this.calculateCosineSimilarity(targetUserVector, entry.getValue());
            similarityMap.put(entry.getKey(), similarity);
        }
        return similarityMap;
    }

    // 가장 유사도가 높은 N명의 User Id를 추출
    private List<Long> findMostSimilarUserList(Map<Long, Double> similarityMap) {
        return similarityMap.entrySet().stream()
            .sorted((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue()))
            .limit(SIMILAR_USER_THRESHOLD_FOR_FIRST_CONTENT_RECOMMENDATION)
            .map(Map.Entry::getKey)
            .toList();
    }

    // 두 벡터간 코사인 유사도 계산
    private double calculateCosineSimilarity(Long[] vectorA, Long[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }

        // 0으로 나누기 방지
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
