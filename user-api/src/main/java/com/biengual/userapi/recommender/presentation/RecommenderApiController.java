package com.biengual.userapi.recommender.presentation;

import static com.biengual.core.response.success.RecommenderSuccessCode.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.biengual.core.response.ResponseEntityFactory;
import com.biengual.core.swagger.SwaggerVoidReturn;
import com.biengual.userapi.recommender.domain.RecommenderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/recommender")
@Tag(name = "Recommender - private API", description = "추천 시스템 어드민 전용 API - 배치로 변경 예정(연결 필요 X)")
public class RecommenderApiController {
    private final RecommenderService recommenderService;

    @PutMapping("/bookmark")
    @Operation(summary = "북마크 추천 시스템 업데이트", description = "북마크 추천 시스템을 업데이트 합니다")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "북마크 추천 시스템 업데이트 성공",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = SwaggerVoidReturn.class))
            }
        ),
        @ApiResponse(responseCode = "500", description = "서버 에러", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<Object> updateBookmarkRecommender() {
        recommenderService.updatePopularBookmarks();

        return ResponseEntityFactory.toResponseEntity(RECOMMENDER_UPDATE_SUCCESS);
    }

}
