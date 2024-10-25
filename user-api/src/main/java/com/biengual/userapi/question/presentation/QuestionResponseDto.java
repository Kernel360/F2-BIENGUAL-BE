package com.biengual.userapi.question.presentation;

import java.util.List;

import com.biengual.userapi.question.domain.QuestionType;

import lombok.Builder;

public class QuestionResponseDto {

	public record View(
		String question,
		String questionKo,
		String answer,
		QuestionType type
	) {
	}

	@Builder
	public record ViewListRes(
		List<View> questionAnswer
	) {
	}
}
