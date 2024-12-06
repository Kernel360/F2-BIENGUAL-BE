package com.biengual.userapi.validator;

import com.biengual.core.annotation.Validator;
import com.biengual.userapi.category.domain.CategoryRepository;
import com.biengual.userapi.recommender.domain.CategoryLearningProgressCustomRepository;
import com.biengual.userapi.user.domain.UserCategoryCustomRepository;
import com.biengual.userapi.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

import static com.biengual.core.constant.RestrictionConstant.*;

@Validator
@RequiredArgsConstructor
public class RecommenderValidator {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CategoryLearningProgressCustomRepository categoryLearningProgressCustomRepository;

    // 첫 번째 컨텐츠 추천을 위한 조건 검증
    public boolean verifyFirstContentRecommendationCondition(Long userId) {
        long categoryCount = categoryRepository.count();
        boolean categoryCountValidity = categoryCount >= MIN_CATEGORY_COUNT_FOR_FIRST_CONTENT_RECOMMENDATION;

        long userCount = userRepository.count();
        boolean userCountValidity = userCount >= MIN_USER_COUNT_FOR_FIRST_CONTENT_RECOMMENDATION;

        boolean learningValidity = categoryLearningProgressCustomRepository.existsByUserId(userId);

        return categoryCountValidity && userCountValidity && learningValidity;
    }

    // 두 번째 컨텐츠 추천을 위한 조건 검증
    public boolean verifySecondContentRecommendationCondition(List<Long> targetUserCategoryIdList) {
        return !targetUserCategoryIdList.isEmpty();
    }

    // 추천 컨텐츠 수가 최대인지 검증
    public boolean verifyRecommendedContentCount(Set<Long> recommendedContentIdSet) {
        return recommendedContentIdSet.size() == MAX_RECOMMENDED_CONTENT_COUNT;
    }
}
