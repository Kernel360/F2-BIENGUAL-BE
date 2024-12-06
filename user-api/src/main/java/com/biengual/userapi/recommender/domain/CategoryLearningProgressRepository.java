package com.biengual.userapi.recommender.domain;

import com.biengual.core.domain.entity.learning.CategoryLearningProgressEntity;
import com.biengual.core.domain.entity.learning.CategoryLearningProgressId;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CategoryLearningProgressEntity의 Repository 계층의 인터페이스
 *
 * @author 문찬욱
 */
public interface CategoryLearningProgressRepository
    extends JpaRepository<CategoryLearningProgressEntity, CategoryLearningProgressId> {
}
