package com.biengual.userapi.question.presentation;

import static com.biengual.userapi.message.response.QuestionResponseCode.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.biengual.userapi.message.ResponseEntityFactory;
import com.biengual.userapi.question.application.QuestionFacade;
import com.biengual.userapi.question.domain.QuestionInfo;
import com.biengual.userapi.swagger.question.SwaggerQuestionView;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/questions")
@Tag(name = "Question - public API", description = "문제 공통 API")
public class QuestionPublicController {
	private final QuestionDtoMapper questionDtoMapper;
	private final QuestionFacade questionFacade;

	@GetMapping("/view/{contentId}")
	@Operation(summary = "문제 조회", description = "컨텐츠 Id를 이용해 문제를 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "문제 조회 성공",
			content = {
				@Content(mediaType = "application/json", schema = @Schema(implementation = SwaggerQuestionView.class))
			}
		),
		@ApiResponse(responseCode = "404", description = "문제 조회 실패", content = @Content(mediaType = "application/json")),
		@ApiResponse(responseCode = "500", description = "서버 에러", content = @Content(mediaType = "application/json"))
	})
	public ResponseEntity<Object> getQuestion(
		@PathVariable
		Long contentId
	) {
		QuestionInfo.DetailInfo info = questionFacade.getQuestions(contentId);
		QuestionResponseDto.ViewListRes response = questionDtoMapper.ofViewRes(info);

		return ResponseEntityFactory.toResponseEntity(QUESTION_VIEW_SUCCESS, response);
	}
}
