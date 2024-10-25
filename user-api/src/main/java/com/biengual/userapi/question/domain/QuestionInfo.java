package com.biengual.userapi.question.domain;

import java.util.List;

import lombok.Builder;

public class QuestionInfo {

	@Builder
	public record Detail(
		String question,
		String questionKo,
		String answer,
		QuestionType type
	) {
	}

	@Builder
	public record DetailInfo(
		List<Detail> questionAnswer
	) {
		public static DetailInfo of(List<Detail> details){
			return DetailInfo.builder()
				.questionAnswer(details)
				.build();
		}
	}
}
