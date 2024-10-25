package com.biengual.userapi.question.domain;

public class QuestionCommand {

	public record Create(
		Long contentId,
		Integer questionNumOfBlank,
		Integer questionNumOfOrder
	) {
	}
}
