package com.biengual.userapi.message.response;

import org.springframework.http.HttpStatus;

import com.biengual.userapi.message.status.BookmarkServiceStatus;
import com.biengual.userapi.message.status.ServiceStatus;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BookmarkResponseCode implements ResponseCode {
	BOOKMARK_VIEW_SUCCESS(HttpStatus.OK, BookmarkServiceStatus.BOOKMARK_VIEW_SUCCESS, "북마크 조회 성공"),
	BOOKMARK_UPDATE_SUCCESS(HttpStatus.OK, BookmarkServiceStatus.BOOKMARK_UPDATE_SUCCESS, "북마크 메모 수정 성공"),
	BOOKMARK_CREATE_SUCCESS(HttpStatus.CREATED, BookmarkServiceStatus.BOOKMARK_CREATE_SUCCESS, "북마크 생성 성공"),
	BOOKMARK_DELETE_SUCCESS(HttpStatus.ACCEPTED, BookmarkServiceStatus.BOOKMARK_DELETE_SUCCESS, "북마크 삭제 성공")
	;

	private final HttpStatus code;
	private final ServiceStatus serviceStatus;
	private final String message;

	@Override
	public HttpStatus getStatus() {
		return code;
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
