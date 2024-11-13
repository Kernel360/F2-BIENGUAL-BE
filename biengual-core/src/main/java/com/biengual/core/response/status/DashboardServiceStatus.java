package com.biengual.core.response.status;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DashboardServiceStatus implements ServiceStatus {
    // success
    RECENT_LEARNING_SUMMARY_VIEW_SUCCESS("U-D-001"),
    RECENT_LEARNING_VIEW_SUCCESS("U-D-002"),
    CATEGORY_LEARNING_VIEW_SUCCESS("U-D-003");

    // failure

    private final String code;

    @Override
    public String getServiceStatus() {
        return code;
    }
}