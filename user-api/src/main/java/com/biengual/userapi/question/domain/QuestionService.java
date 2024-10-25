package com.biengual.userapi.question.domain;

public interface QuestionService {
	void createQuestion(QuestionCommand.Create command);

	QuestionInfo.DetailInfo getQuestions(Long contentId);
}
