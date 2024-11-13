package com.biengual.userapi.dashboard.domain;

/**
 * Dashboard 도메인의 Service 계층의 인터페이스
 */
public interface DashboardService {
    DashboardInfo.RecentLearningList getRecentLearning(Long userId);

    DashboardInfo.CategoryLearningList getCategoryLearning(Long userId, String date);
}
