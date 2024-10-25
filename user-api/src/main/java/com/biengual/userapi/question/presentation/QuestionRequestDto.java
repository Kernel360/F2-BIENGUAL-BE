package com.biengual.userapi.question.presentation;

public class QuestionRequestDto {
	public record CreateReq(
		Integer questionNumOfBlank,
		Integer questionNumOfOrder
	) {
	}
}