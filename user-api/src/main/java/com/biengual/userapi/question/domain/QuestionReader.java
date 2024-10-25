package com.biengual.userapi.question.domain;

import java.util.List;

public interface QuestionReader {
	List<QuestionInfo.Detail> getQuestions(Long contentId);
}
