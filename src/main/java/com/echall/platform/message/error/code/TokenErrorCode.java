package com.echall.platform.message.error.code;

import org.springframework.http.HttpStatus;

import com.echall.platform.message.status.ServiceStatus;
import com.echall.platform.message.status.TokenServiceStatus;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TokenErrorCode implements ErrorCode {
	REFRESH_TOKEN_NOT_FOUND(
		HttpStatus.NOT_FOUND, TokenServiceStatus.REFRESH_TOKEN_NOT_FOUND, "리프레시 토큰 확인 불가"
	)

	;

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