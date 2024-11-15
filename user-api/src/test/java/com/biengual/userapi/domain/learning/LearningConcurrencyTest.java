package com.biengual.userapi.domain.learning;

import com.biengual.core.domain.entity.category.CategoryEntity;
import com.biengual.core.domain.entity.content.ContentEntity;
import com.biengual.core.domain.entity.learning.CategoryLearningHistoryEntity;
import com.biengual.core.domain.entity.learning.RecentLearningHistoryEntity;
import com.biengual.userapi.content.domain.ContentRepository;
import com.biengual.userapi.learning.domain.LearningCommand;
import com.biengual.userapi.learning.domain.LearningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.biengual.userapi.domain.category.CategoryTestFixture.TestCategoryEntity.createCategoryEntity;
import static com.biengual.userapi.domain.content.ContentTestFixture.TestContentEntity.createContentEntity;
import static com.biengual.userapi.domain.learning.LearningTestFixture.TestCommandRecordLearningRate.createCommandRecordLearningRate;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("학습 도메인 동시성 테스트")
public class LearningConcurrencyTest {

    private Logger log = LoggerFactory.getLogger(LearningConcurrencyTest.class);
    private Long initContentId;

    @Autowired
    LearningService learningService;

    @Autowired
    ContentRepository contentRepository;

    @Autowired
    TestRecentLearningHistoryRepository testRecentLearningHistoryRepository;

    @Autowired
    TestCategoryLearningHistoryRepository testCategoryLearningHistoryRepository;

    @BeforeEach
    void init() {
        CategoryEntity category = createCategoryEntity().build().get();

        ContentEntity content = createContentEntity()
            .category(category)
            .build()
            .get();

        ContentEntity savedContent = contentRepository.save(content);

        initContentId = savedContent.getId();
    }

    @Test
    @DisplayName("컨텍스트 로드 테스트")
    void contextLoads() {
    }

    @Test
    @DisplayName("LearningService의 recordLearningRate 메서드 동시 호출 시 최근 학습 내역 및 카테고리별 학습 내역 동시성 테스트")
    void recordLearningRate_ShouldEnsureConcurrency_WhenCalledSimultaneously() throws InterruptedException {
        // given
        int threadCount = 100;

        LearningCommand.RecordLearningRate command = createCommandRecordLearningRate()
            .contentId(initContentId)
            .build()
            .get();

        // when
        createExecutorService(threadCount, () -> learningService.recordLearningRate(command));

        // then
        List<RecentLearningHistoryEntity> recentLearningHistory =
            testRecentLearningHistoryRepository.findAllByUserIdAndContentId(command.userId(), command.contentId());
        List<CategoryLearningHistoryEntity> categoryLearningHistory =
            testCategoryLearningHistoryRepository.findAllByUserId(command.userId());

        log.info("recentLearningHistory size: {}", recentLearningHistory.size());
        log.info("categoryLearningHistory size: {}", categoryLearningHistory.size());

        assertThat(recentLearningHistory).hasSize(1);
        assertThat(categoryLearningHistory).hasSize(1);
    }

    // Internal Method =================================================================================================

    // ExecutorService 캡슐화
    private void createExecutorService(int threadCount, Runnable task) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    task.run();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();
    }
}
