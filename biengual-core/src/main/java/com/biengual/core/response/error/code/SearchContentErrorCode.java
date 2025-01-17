package com.biengual.core.response.error.code;

import org.springframework.http.HttpStatus;

import com.biengual.core.response.status.SearchContentServiceStatus;
import com.biengual.core.response.status.ServiceStatus;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SearchContentErrorCode implements ErrorCode {
    SEARCH_CONTENT_SAVE_FAILED(
        HttpStatus.INTERNAL_SERVER_ERROR,
        SearchContentServiceStatus.SEARCH_CONTENT_SAVE_FAILED,
        "OpenSearch 컨텐츠 저장 실패"
    ),
    SEARCH_CONTENT_DELETE_FAILED(
        HttpStatus.INTERNAL_SERVER_ERROR,
        SearchContentServiceStatus.SEARCH_CONTENT_DELETE_FAILED,
        "OpenSearch 컨텐츠 삭제 실패"
    ),
    OPEN_SEARCH_FAILED(
        HttpStatus.NOT_FOUND,
        SearchContentServiceStatus.OPEN_SEARCH_FAILED,
        "OpenSearch 검색 실패"
    );

    private final HttpStatus httpStatus;
    private final ServiceStatus serviceStatus;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return httpStatus;
    }

    @Override
    public String getCode() {
        return serviceStatus.getServiceStatus();
    }

    @Override
    public String getMessage() {
        return message;
    }
}
