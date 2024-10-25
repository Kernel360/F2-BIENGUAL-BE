package com.biengual.userapi.question.application;

import com.biengual.userapi.annotation.Facade;
import com.biengual.userapi.question.domain.QuestionCommand;
import com.biengual.userapi.question.domain.QuestionInfo;
import com.biengual.userapi.question.domain.QuestionService;

import lombok.RequiredArgsConstructor;

@Facade
@RequiredArgsConstructor
public class QuestionFacade {
	private final QuestionService questionService;

	public void createQuestion(QuestionCommand.Create command) {
		questionService.createQuestion(command);
	}

	public QuestionInfo.DetailInfo getQuestions(Long contentId) {
		return questionService.getQuestions(contentId);
	}
}
