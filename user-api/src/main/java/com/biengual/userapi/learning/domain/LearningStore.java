package com.biengual.userapi.learning.domain;

import com.biengual.core.domain.entity.content.ContentEntity;

/**
 * UserLearningHistory 도메인의 DataProvider 계층의 인터페이스
 *
 * @author 문찬욱
 */
public interface LearningStore {

    void recordLearningHistory(LearningCommand.RecordLearningRate command);

    void recordRecentLearningHistory(LearningCommand.RecordLearningRate command);

    void recordCategoryLearningHistory(LearningCommand.RecordLearningRate command, ContentEntity content);

    void updateCategoryLearningProgress(LearningCommand.RecordLearningRate command, ContentEntity content);
}
