package com.biengual.userapi.learning.infrastructure;

import com.biengual.core.annotation.DataProvider;
import com.biengual.core.domain.entity.content.ContentEntity;
import com.biengual.core.domain.entity.learning.CategoryLearningProgressEntity;
import com.biengual.core.domain.entity.learning.CategoryLearningProgressId;
import com.biengual.core.domain.entity.learning.RecentLearningHistoryEntity;
import com.biengual.userapi.learning.domain.*;
import com.biengual.userapi.recommender.domain.CategoryLearningProgressRepository;
import lombok.RequiredArgsConstructor;

@DataProvider
@RequiredArgsConstructor
public class LearningStoreImpl implements LearningStore {
    private final LearningHistoryRepository learningHistoryRepository;
    private final RecentLearningHistoryRepository recentLearningHistoryRepository;
    private final CategoryLearningHistoryRepository categoryLearningHistoryRepository;
    private final CategoryLearningProgressRepository categoryLearningProgressRepository;

    // 모든 학습 내역 쌓기
    @Override
    public void recordLearningHistory(LearningCommand.RecordLearningRate command) {
        learningHistoryRepository.save(command.toLearningHistoryEntity());
    }

    // 최근 학습 내역 쌓기
    @Override
    public void recordRecentLearningHistory(LearningCommand.RecordLearningRate command) {
        RecentLearningHistoryEntity userLearningHistory =
            recentLearningHistoryRepository.findByUserIdAndContentId(command.userId(), command.contentId())
                .map(history -> {
                    history.record(command.learningRate(), command.learningTime());
                    return history;
                })
                .orElseGet(command::toUserLearningHistoryEntity);

        recentLearningHistoryRepository.save(userLearningHistory);
    }

    // 카테고리별 학습 내역 쌓기
    @Override
    public void recordCategoryLearningHistory(LearningCommand.RecordLearningRate command, ContentEntity content) {
        Long categoryId = content.getCategory().getId();

        categoryLearningHistoryRepository.save(command.toCategoryLearningHistoryEntity(categoryId));
    }

    // 카테고리별 학습 상황 업데이트
    @Override
    public void updateCategoryLearningProgress(LearningCommand.RecordLearningRate command, ContentEntity content) {
        Long categoryId = content.getCategory().getId();

        CategoryLearningProgressId categoryLearningProgressId =
            CategoryLearningProgressId.create(command.userId(), categoryId);

        CategoryLearningProgressEntity categoryLearningProgress =
            categoryLearningProgressRepository.findById(categoryLearningProgressId)
                .orElseGet(() -> command.toCategoryLearningProgressEntity(categoryLearningProgressId));

        categoryLearningProgress.updateProgress(command.learningRate());
    }
}
