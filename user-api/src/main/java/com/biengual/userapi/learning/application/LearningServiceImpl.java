package com.biengual.userapi.learning.application;

import com.biengual.core.annotation.RedisDistributedLock;
import com.biengual.core.domain.entity.content.ContentEntity;
import com.biengual.userapi.content.domain.ContentReader;
import com.biengual.userapi.learning.domain.LearningCommand;
import com.biengual.userapi.learning.domain.LearningService;
import com.biengual.userapi.learning.domain.LearningStore;
import com.biengual.userapi.validator.LearningValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LearningServiceImpl implements LearningService {
    private final LearningValidator learningValidator;
    private final ContentReader contentReader;
    private final LearningStore learningStore;

    // TODO: 추후 learningStore 메서드 간의 비동기를 생각해볼 수 있을 것 같습니다.
    // 학습률 업데이트
    @Override
    @RedisDistributedLock(key = "#command.userId() + \":\" + #command.contentId()")
    public void recordLearningRate(LearningCommand.RecordLearningRate command) {
        ContentEntity content = contentReader.findLearnableContent(command.contentId(), command.userId());

        if (!learningValidator.verifyAlreadyLearningInMonth(
            command.userId(), command.contentId(), command.learningTime()
        )) {
            learningStore.recordCategoryLearningHistory(command, content);
        }

        learningStore.recordLearningHistory(command);

        learningStore.recordRecentLearningHistory(command);

        learningStore.updateCategoryLearningProgress(command, content);
    }
}
