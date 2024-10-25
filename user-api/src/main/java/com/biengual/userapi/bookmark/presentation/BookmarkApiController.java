package com.biengual.userapi.bookmark.presentation;

import static com.biengual.userapi.message.response.BookmarkResponseCode.*;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.biengual.userapi.bookmark.application.BookmarkFacade;
import com.biengual.userapi.bookmark.domain.BookmarkCommand;
import com.biengual.userapi.bookmark.domain.BookmarkInfo;
import com.biengual.userapi.message.ResponseEntityFactory;
import com.biengual.userapi.oauth2.domain.info.OAuth2UserPrincipal;
import com.biengual.userapi.swagger.SwaggerVoidReturn;
import com.biengual.userapi.swagger.bookmark.SwaggerBookmarkList;
import com.biengual.userapi.swagger.bookmark.SwaggerBookmarkMyList;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RequestMapping("/api/bookmark")
@RequiredArgsConstructor
@RestController
@Tag(name = "Bookmark - private API", description = "북마크 회원전용 API")
public class BookmarkApiController {
	private final BookmarkFacade bookmarkFacade;
	private final BookmarkDtoMapper bookmarkDtoMapper;

	@GetMapping("/view/{contentId}")
	@Operation(summary = "게시글 북마크 조회", description = "회원이 등록한 게시글 관련 북마크 목록을 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "북마크 조회 성공",
			content = {
				@Content(mediaType = "application/json", schema = @Schema(implementation = SwaggerBookmarkList.class))}
		),
		@ApiResponse(responseCode = "404", description = "유저 조회 실패", content = @Content),
		@ApiResponse(responseCode = "500", description = "서버 에러", content = @Content)
	})
	public ResponseEntity<Object> getBookmarks(
		@AuthenticationPrincipal
		OAuth2UserPrincipal principal,
		@PathVariable
		Long contentId
	) {
		BookmarkCommand.GetByContents command = bookmarkDtoMapper.doGetByContents(contentId, principal);
		BookmarkResponseDto.ContentListRes response
			= bookmarkDtoMapper.ofContentListRes(bookmarkFacade.getBookmarks(command));

		return ResponseEntityFactory.toResponseEntity(BOOKMARK_VIEW_SUCCESS, response);
	}

	@GetMapping("/view")
	@Operation(summary = "북마크 전체 조회", description = "회원이 등록한 모든 북마크 목록을 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "북마크 조회 성공",
			content = {
				@Content(mediaType = "application/json", schema = @Schema(implementation = SwaggerBookmarkMyList.class))}
		),
		@ApiResponse(responseCode = "500", description = "서버 에러", content = @Content)
	})
	public ResponseEntity<Object> getAllBookmarks(
		@AuthenticationPrincipal
		OAuth2UserPrincipal principal
	) {
		BookmarkInfo.MyListInfo info = bookmarkFacade.getAllBookmarks(principal.getId());
		BookmarkResponseDto.MyListRes response
			= bookmarkDtoMapper.ofMyListRes(info);

		return ResponseEntityFactory.toResponseEntity(BOOKMARK_VIEW_SUCCESS, response);
	}

	@PostMapping("/create/{contentId}")
	@Operation(summary = "북마크 생성", description = "회원이 북마크를 생성합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "북마크 생성 성공",
			content = {
				@Content(mediaType = "application/json", schema = @Schema(implementation = SwaggerVoidReturn.class))
			}),
		@ApiResponse(responseCode = "404", description = "유저 혹은 컨텐츠 조회 실패", content = @Content),
		@ApiResponse(responseCode = "500", description = "서버 에러", content = @Content)
	})
	public ResponseEntity<Object> createBookmark(
		@AuthenticationPrincipal
		OAuth2UserPrincipal principal,
		@PathVariable
		Long contentId,
		@RequestBody
		BookmarkRequestDto.CreateReq request
	) {
		BookmarkCommand.Create command = bookmarkDtoMapper.doCreate(contentId, request, principal);
		bookmarkFacade.createBookmark(command);

		return ResponseEntityFactory.toResponseEntity(BOOKMARK_CREATE_SUCCESS);
	}

	@PutMapping("/update/{contentId}")
	@Operation(summary = "북마크 메모 수정", description = "회원이 북마크 메모를 수정합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "북마크 메모 수정 성공",
			content = {
				@Content(mediaType = "application/json", schema = @Schema(implementation = SwaggerBookmarkList.class))
			}),
		@ApiResponse(responseCode = "404", description = "북마크 조회 실패", content = @Content),
		@ApiResponse(responseCode = "500", description = "서버 에러", content = @Content)
	})
	public ResponseEntity<Object> updateBookmark(
		@AuthenticationPrincipal
		OAuth2UserPrincipal principal,
		@PathVariable
		Long contentId,
		@RequestBody
		BookmarkRequestDto.UpdateReq request
	) {
		BookmarkCommand.Update command = bookmarkDtoMapper.doUpdate(contentId, request, principal);
		BookmarkResponseDto.ContentList response = bookmarkDtoMapper.ofContentList(
			bookmarkFacade.updateBookmark(command));

		return ResponseEntityFactory.toResponseEntity(BOOKMARK_UPDATE_SUCCESS, response);
	}

	@DeleteMapping("/delete/{bookmarkId}")
	@Operation(summary = "북마크 삭제", description = "회원이 북마크를 삭제합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "북마크 삭제 성공",
			content = {
				@Content(mediaType = "application/json", schema = @Schema(implementation = SwaggerVoidReturn.class))
			}),
		@ApiResponse(responseCode = "404", description = "북마크 조회 실패", content = @Content),
		@ApiResponse(responseCode = "500", description = "서버 에러", content = @Content)
	})
	public ResponseEntity<Object> deleteBookmark(
		@AuthenticationPrincipal
		OAuth2UserPrincipal principal,
		@PathVariable
		Long bookmarkId
	) {
		BookmarkCommand.Delete command = bookmarkDtoMapper.doDelete(bookmarkId, principal);
		bookmarkFacade.deleteBookmark(command);

		return ResponseEntityFactory.toResponseEntity(BOOKMARK_DELETE_SUCCESS);
	}

}
