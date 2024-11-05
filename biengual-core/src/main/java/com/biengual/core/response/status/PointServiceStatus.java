package com.biengual.core.response.status;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PointServiceStatus implements ServiceStatus {
    // success


    // failure
    POINT_NOT_FOUND("U-P-901"),
    POINT_NEVER_MINUS("U-P-902")
    ;

    private final String code;

    @Override
    public String getServiceStatus() {
        return code;
    }
}