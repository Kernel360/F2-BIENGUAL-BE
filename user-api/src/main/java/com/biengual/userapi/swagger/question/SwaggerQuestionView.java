package com.biengual.userapi.swagger.question;

import java.util.List;
import java.util.Map;

import com.biengual.userapi.question.presentation.QuestionResponseDto;
import com.biengual.userapi.swagger.SwaggerReturnInterface;

public class SwaggerQuestionView
	extends SwaggerReturnInterface<Map<String, List<QuestionResponseDto.View>>> {
}
