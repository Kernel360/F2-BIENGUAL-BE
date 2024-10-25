package com.biengual.userapi.question.domain;

public interface QuestionStore {
	void createQuestion(QuestionCommand.Create command);
}
