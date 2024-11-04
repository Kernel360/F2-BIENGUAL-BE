package com.biengual.userapi.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import com.biengual.core.domain.entity.pointdatamart.PointDataMart;
import com.biengual.core.domain.entity.pointhistory.PointHistoryEntity;
import com.biengual.userapi.pointdatamart.domain.PointDataMartRepository;
import com.biengual.userapi.pointhistory.domain.PointHistoryRepository;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class PointDataMartJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final PointHistoryRepository pointHistoryRepository;
    private final PointDataMartRepository pointDataMartRepository;

    /**
     * Job 생성 : pointDataMartJob
     * Step 포함 : pointDataMartStep
     */
    @Bean
    public Job pointDataMartJob() {
        return new JobBuilder("pointDataMartJob", jobRepository)
            .start(pointDataMartStep())
            .build();
    }

    /**
     * chunk 기반 Step 처리
     * 한 번에 1000개의 아이템 처리
     */
    @Bean
    public Step pointDataMartStep() {
        return new StepBuilder("pointDataMartStep", jobRepository)
            .<PointHistoryEntity, PointDataMart>chunk(1000, transactionManager)
            .reader(pointHistoryReader(
                LocalDateTime.of(LocalDate.now().minusDays(5), LocalTime.of(0, 0))
            ))
            .processor(pointDataMartProcessor())
            .writer(pointDataMartWriter())
            .build();
    }

    /**
     * pointHistoryRepository.findByProcessedFalse 를 사용하여 PointHistoryEntity 조회
     * 처리 되지 않은 포인트 내역(어제 00시 이후이고 `processed = false` 인 항목) 조회
     * 날짜로만 확인하면 시간이 무조건 일정하다는 보장이 없음
     */
    @Bean
    @StepScope
    public RepositoryItemReader<PointHistoryEntity> pointHistoryReader(
        @Value("#{jobParameters['createdAt']}") LocalDateTime createdAtString
    ) {

        return new RepositoryItemReaderBuilder<PointHistoryEntity>()
            .name("pointHistoryReader")
            .repository(pointHistoryRepository)
            .methodName("findByProcessedFalseAndCreatedAtAfter")
            .pageSize(100)
            .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
            .arguments(Arrays.asList(createdAtString, PageRequest.of(0, 100)))
            .build();
    }

    /**
     * PointHistoryEntity 에 대한 PointDataMart 조회 및 업데이트(`processed = true`)
     */
    @Bean
    public ItemProcessor<PointHistoryEntity, PointDataMart> pointDataMartProcessor() {
        return history -> {
            PointDataMart dataMart = pointDataMartRepository.findByUserId(history.getUserId())
                .orElseGet(() -> PointDataMart.createPointDataMart(history.getUserId()));

            if (history.getCreatedAt().isAfter(
                LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 0)))
            ) {
                dataMart.updateByPointHistory(history);
                history.updateProcessed(true);
            }

            return dataMart;
        };
    }

    /**
     * CompositeItemWriter
     */
    @Bean
    public CompositeItemWriter<PointHistoryEntity> compositeItemWriter() {
        CompositeItemWriter<PointHistoryEntity> writer = new CompositeItemWriter<>();
        writer.setDelegates(Collections.singletonList(pointHistoryWriter()));
        return writer;
    }

    /**
     * JpaItemWriter 를 이용해 PointDataMart 저장
     */
    @Bean
    public JpaItemWriter<PointDataMart> pointDataMartWriter() {
        return new JpaItemWriterBuilder<PointDataMart>()
            .entityManagerFactory(entityManagerFactory)
            .build();
    }

    /**
     * JpaItemWriter 를 이용해 PointHistoryEntity 저장
     */
    @Bean
    public JpaItemWriter<PointHistoryEntity> pointHistoryWriter() {
        return new JpaItemWriterBuilder<PointHistoryEntity>()
            .entityManagerFactory(entityManagerFactory)
            .build();
    }

}