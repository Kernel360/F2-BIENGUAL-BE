package com.biengual.userapi.metadata.infrastructure;

import com.biengual.core.annotation.DataProvider;
import com.biengual.core.domain.entity.content.ContentLevelFeedbackDataMart;
import com.biengual.core.domain.entity.metadata.AggregationMetadataEntity;
import com.biengual.core.util.PeriodUtil;
import com.biengual.core.util.TimeRange;
import com.biengual.userapi.content.domain.ContentInfo;
import com.biengual.userapi.content.domain.ContentLevelFeedbackDataMartCustomRepository;
import com.biengual.userapi.content.domain.ContentLevelFeedbackDataMartRepository;
import com.biengual.userapi.content.domain.ContentLevelFeedbackHistoryCustomRepository;
import com.biengual.userapi.metadata.domain.AggregationMetadataRepository;
import com.biengual.userapi.metadata.domain.MetadataStore;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import static com.biengual.core.constant.ServiceConstant.CONTENT_LEVEL_FEEDBACK_HISTORY_TABLE;

@DataProvider
@RequiredArgsConstructor
public class MetadataStoreImpl implements MetadataStore {
    private final AggregationMetadataRepository aggregationMetadataRepository;
    private final ContentLevelFeedbackHistoryCustomRepository contentLevelFeedbackHistoryCustomRepository;
    private final ContentLevelFeedbackDataMartRepository contentLevelFeedbackDataMartRepository;
    private final ContentLevelFeedbackDataMartCustomRepository contentLevelFeedbackDataMartCustomRepository;

    // ContentLevelFeedbackHistory 집계한 후 집계된 ContentId들을 반환
    @Override
    public Set<Long> aggregateContentLevelFeedbackHistory() {
        Set<Long> aggregatedContentIdList = new HashSet<>();

        AggregationMetadataEntity aggregationMetadata =
            this.findAggregationMetadata(CONTENT_LEVEL_FEEDBACK_HISTORY_TABLE);

        LocalDateTime nextAggTime = aggregationMetadata.getNextAggTime();

        Queue<TimeRange> aggregationPeriodQueue = PeriodUtil.getAggregationPeriodQueue(aggregationMetadata);

        // TODO: 현재는 순차 처리이고, 추후 개선하여 성능 비교하면 좋을 것 같습니다.
        // 각 집계 기간
        while (!aggregationPeriodQueue.isEmpty()) {
            TimeRange timeRange = aggregationPeriodQueue.poll();

            List<ContentInfo.AggregatedLevelFeedback> aggregatedLevelFeedbackList =
                contentLevelFeedbackHistoryCustomRepository.countContentLevelsGroupByContentIdInTimeRange(timeRange);

            // 각 집계된 Content
            for (ContentInfo.AggregatedLevelFeedback aggregatedLevelFeedback : aggregatedLevelFeedbackList) {
                // 처음 집계된 Content
                if (!contentLevelFeedbackDataMartRepository.existsById(aggregatedLevelFeedback.contentId())) {
                    ContentLevelFeedbackDataMart contentLevelFeedbackDataMart =
                        aggregatedLevelFeedback.toContentLevelFeedbackDataMart();

                    contentLevelFeedbackDataMartRepository.save(contentLevelFeedbackDataMart);

                // 집계된 적 있는 Content
                } else {
                    contentLevelFeedbackDataMartCustomRepository
                        .updateByAggregatedLevelFeedbackInfo(aggregatedLevelFeedback);
                }

                aggregatedContentIdList.add(aggregatedLevelFeedback.contentId());
            }

            nextAggTime = timeRange.end();
        }

        // 집계 완료 후, AggregationMetadata의 해당 TableName에 대한 AggregationTime 업데이트
        aggregationMetadata.updateNextAggTime(nextAggTime);

        return aggregatedContentIdList;
    }

    // Internal Method =================================================================================================

    // TableName에 맞는 집계 메타데이터를 얻는 메소드
    private AggregationMetadataEntity findAggregationMetadata(String tableName) {
        return aggregationMetadataRepository.findByTableName(tableName)
            .orElseGet(() -> {
                AggregationMetadataEntity aggregationMetadata =
                    AggregationMetadataEntity.createEntityByTableName(tableName);

                return aggregationMetadataRepository.save(aggregationMetadata);
            });
    }
}
