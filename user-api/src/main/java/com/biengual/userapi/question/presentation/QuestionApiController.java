package com.biengual.userapi.question.presentation;

import static com.biengual.userapi.message.response.QuestionResponseCode.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.biengual.userapi.message.ResponseEntityFactory;
import com.biengual.userapi.question.application.QuestionFacade;
import com.biengual.userapi.question.domain.QuestionCommand;
import com.biengual.userapi.swagger.SwaggerVoidReturn;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/questions")
@Tag(name = "Question - private API", description = "문제 생성 회원전용 API")
public class QuestionApiController {
	private final QuestionFacade questionFacade;
	private final QuestionDtoMapper questionDtoMapper;

	@PostMapping("/create/{contentId}")
	@Operation(summary = "어드민 - 문제 생성", description = "어드민 회원이 컨텐츠에 대한 문제를 새로 등록합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "문제 생성 성공",
			content = {
				@Content(mediaType = "application/json", schema = @Schema(implementation = SwaggerVoidReturn.class))
			}
		),
		@ApiResponse(responseCode = "500", description = "서버 에러", content = @Content(mediaType = "application/json"))
	})
	public ResponseEntity<Object> createQuestion(
		@PathVariable
		Long contentId,
		@RequestBody
		QuestionRequestDto.CreateReq request
	) {
		QuestionCommand.Create command = questionDtoMapper.doCreate(contentId, request);
		questionFacade.createQuestion(command);

		return ResponseEntityFactory.toResponseEntity(QUESTION_CREATE_SUCCESS);
	}

}
