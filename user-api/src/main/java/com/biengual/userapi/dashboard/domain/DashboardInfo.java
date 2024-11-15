package com.biengual.userapi.dashboard.domain;

import com.biengual.core.enums.PointReason;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.biengual.core.enums.ContentType;

import lombok.Builder;

public class DashboardInfo {

    public record RecentLearning(
        Long contentId,
        String title,
        String thumbnailUrl,
        ContentType contentType,
        String preScripts,
        String category,
        Integer videoDurationInSeconds,
        Integer hits,
        Boolean isScrapped,
        BigDecimal currentLearningRate,
        BigDecimal completedLearningRate
    ) {
    }

    @Builder
    public record RecentLearningList(
        List<RecentLearning> recentLearningPreview
    ) {
        public static RecentLearningList of(List<RecentLearning> recentLearningList) {
            return RecentLearningList.builder()
                .recentLearningPreview(recentLearningList)
                .build();
        }
    }

    public record CategoryLearning(
        Long categoryId,
        String categoryName,
        Long count
    ) {
    }

    @Builder
    public record CategoryLearningList(
        Long totalCount,
        List<CategoryLearning> categoryLearningList
    ) {
        public static CategoryLearningList of(Long totalCount, List<CategoryLearning> categoryLearningList) {
            return CategoryLearningList.builder()
                .totalCount(totalCount)
                .categoryLearningList(categoryLearningList)
                .build();
        }
    }

    public record RecentLearningSummary(
        String title,
        BigDecimal completedLearningRate
    ) {
    }

    public record MissionStatus(
        Boolean oneContent,
        Boolean bookmark,
        Boolean quiz,
        Integer count
    ) {
    }

    public record MissionHistory(
        LocalDateTime date,
        MissionStatus missionStatus
    ) {
    }

    @Builder
    public record MissionCalendar(
        List<MissionHistory> missionHistoryList
    ) {
        public static MissionCalendar of(List<MissionHistory> missionHistoryList) {
            return MissionCalendar.builder()
                .missionHistoryList(missionHistoryList)
                .build();
        }
    }

    public record QuestionSummary(
        Double firstTryCorrectRate,
        Double reTryCorrectRate
    ) {
    }
  
    @Builder
    public record PointRecord(
        PointReason reason,
        Long point
    ) {
        public static PointRecord of(PointReason reason, Long point) {
            return PointRecord.builder()
                .reason(reason)
                .point(point)
                .build();
        }
    }

    public record DailyPointHistory(
        LocalDate date,
        List<PointRecord> pointsHistory
    ) {
    }

    @Builder
    public record MonthlyPointHistory(
        Long currentPoint,
        List<DailyPointHistory> dailyPointHistoryList
    ) {
        public static MonthlyPointHistory of(Long currentPoint, List<DailyPointHistory> dailyPointHistoryList) {
            return MonthlyPointHistory.builder()
                .currentPoint(currentPoint)
                .dailyPointHistoryList(dailyPointHistoryList)
                .build();
        }
    }
}
