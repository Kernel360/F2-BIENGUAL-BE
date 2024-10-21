package com.echall.platform.bookmark.service;

import static com.echall.platform.message.error.code.BookmarkErrorCode.*;
import static com.echall.platform.message.error.code.ContentErrorCode.*;
import static com.echall.platform.message.error.code.UserErrorCode.*;

import java.util.Comparator;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.echall.platform.bookmark.domain.dto.BookmarkRequestDto;
import com.echall.platform.bookmark.domain.dto.BookmarkResponseDto;
import com.echall.platform.bookmark.domain.entity.BookmarkEntity;
import com.echall.platform.bookmark.repository.BookmarkRepository;
import com.echall.platform.content.domain.entity.ContentDocument;
import com.echall.platform.content.domain.enums.ContentType;
import com.echall.platform.content.repository.ContentRepository;
import com.echall.platform.content.repository.ContentScriptRepository;
import com.echall.platform.message.error.exception.CommonException;
import com.echall.platform.script.domain.entity.YoutubeScript;
import com.echall.platform.user.domain.entity.UserEntity;
import com.echall.platform.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {
	private final UserRepository userRepository;
	private final BookmarkRepository bookmarkRepository;
	private final ContentRepository contentRepository;
	private final ContentScriptRepository contentScriptRepository;

	@Override
	@Transactional(readOnly = true)
	public List<BookmarkResponseDto.BookmarkListResponseDto> getBookmarks(String email, Long contentId) {
		UserEntity user = userRepository.findByEmail(email)
			.orElseThrow(() -> new CommonException(USER_NOT_FOUND));

		List<BookmarkEntity> bookmarks = user.getBookmarks()
			.stream()
			.filter(bookmarkEntity -> bookmarkEntity.getScriptIndex().equals(contentId))
			.sorted(Comparator.comparing(BookmarkEntity::getUpdatedAt).reversed())
			.toList();

		return bookmarks.stream()
			.map(BookmarkResponseDto.BookmarkListResponseDto::of)
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<BookmarkResponseDto.BookmarkMyListResponseDto> getAllBookmarks(Long userId) {
		List<BookmarkEntity> bookmarks = bookmarkRepository.getAllBookmarks(userId);

		return bookmarks.stream()
			.map(bookmark -> BookmarkResponseDto.BookmarkMyListResponseDto.of(
				bookmark,
				contentRepository.findContentTypeById(bookmark.getScriptIndex()),
				contentRepository.findTitleById(bookmark.getScriptIndex())
			)).toList();
	}

	@Override
	@Transactional
	public BookmarkResponseDto.BookmarkCreateResponse createBookmark(
		String email, BookmarkRequestDto.BookmarkCreateRequest bookmarkRequestDto, Long contentId
	) {

		UserEntity user = userRepository.findByEmail(email)
			.orElseThrow(() -> new CommonException(USER_NOT_FOUND));
		ContentDocument content = contentScriptRepository.findContentDocumentById(
			new ObjectId(contentRepository.findMongoIdByContentId(contentId))
		).orElseThrow(() -> new CommonException(CONTENT_NOT_FOUND));

		if (bookmarkRepository.isBookmarkAlreadyPresent(contentId, bookmarkRequestDto, user.getId())) {
			throw new CommonException(BOOKMARK_ALREADY_EXISTS);
		}

		BookmarkEntity bookmark = BookmarkEntity.builder()
			.scriptIndex(contentId)
			.sentenceIndex(bookmarkRequestDto.sentenceIndex())
			.wordIndex(bookmarkRequestDto.wordIndex())
			.description(bookmarkRequestDto.description())
			.detail(extractDetail(bookmarkRequestDto, content))
			.startTimeInSecond(extractStartTime(
				bookmarkRequestDto, contentRepository.findContentTypeById(contentId), content)
			)
			.build();

		/*
		아래의 코드에서 UserEntity에서 BookmarkEntity와 연관관계를 @OneToMany로 맺고 있어서 아래와 같은 코드로 bookmark 테이블에
		user_id가 저장되고 있습니다.
		하지만 user.updateUserBookmark에서 해당 유저의 bookmark를 전부 불러온 다음에 저장하는 것은 비효율적으로 생각해서
		개선하려고 합니다.
		그러나, UserEntity에서 연관관계의 주체가 된 시점에서 bookmark를 전부 불러오지 않고 저장하는 방법이 잘 떠올리지 않아서,
		BookmarkEntity에도 연관관계 @ManyToOne 을 추가하여 bookmark를 생성할 때 user_id를 넣어 save로만 할 수 있게 하려고 했습니다.
		그런데, user_id를 인증 객체(OAuth2UserPrincipal) 받아올 수 있기에, UserEntity 대신 user_id를 넣어서 저장할 수 있지
		않을까라는 생각이 들었습니다.
		그래서 이를 위한 방법으로 2가지가 떠올랐는데, 어떤 걸 사용하면 좋은지, 더 좋은 방법이 있는지 궁금합니다.
		(방법 2.는 제출 시간상 정상적으로 동작하는지 확인하지 못했습니다. ㅠㅠ) -> 이 후 따로 확인 해보니 정상적으로 동작했습니다.
		방법 1. BookmarkEntity에서 연관관계 @ManyToOne UserEntity를 삭제하고 Long userId 로 변경
		방법 2. BookmarkEntity에서 연관관계 @ManyToOne UserEntity를 유지하고 userId만 넣은 User 객체를 만들어 넣은 다음 save
		 */
		bookmarkRepository.save(bookmark);
		user.updateUserBookmark(bookmark);

		return BookmarkResponseDto.BookmarkCreateResponse.of(bookmark);
	}

	@Override
	@Transactional
	public BookmarkResponseDto.BookmarkListResponseDto updateBookmark(
		BookmarkRequestDto.BookmarkUpdateRequest bookmarkRequestDto, Long contentId
	) {

		BookmarkEntity bookmark = bookmarkRepository.findById(bookmarkRequestDto.bookmarkId())
			.orElseThrow(() -> new CommonException(BOOKMARK_NOT_FOUND));

		bookmark.updateDescription(bookmarkRequestDto);

		return BookmarkResponseDto.BookmarkListResponseDto.of(bookmark);
	}

	@Override
	@Transactional
	public void deleteBookmark(Long userId, Long bookmarkId) {
		bookmarkRepository.deleteBookmark(userId, bookmarkId);
	}

	// Internal Methods ------------------------------------------------------------------------------------------------

	private String extractDetail(
		BookmarkRequestDto.BookmarkCreateRequest bookmarkRequestDto, ContentDocument content
	) {
		return truncate(bookmarkRequestDto.wordIndex() == null ?
				content.getScripts().get(Math.toIntExact(bookmarkRequestDto.sentenceIndex())).getEnScript() :
				content.getScripts().get(Math.toIntExact(bookmarkRequestDto.sentenceIndex())).getEnScript()
					.split(" ")[Math.toIntExact(bookmarkRequestDto.wordIndex())],
			255);
	}

	private Double extractStartTime(
		BookmarkRequestDto.BookmarkCreateRequest bookmarkRequestDto,
		ContentType contentTypeById, ContentDocument content
	) {
		if (contentTypeById.equals(ContentType.READING)) {
			return null;
		}

		return ((YoutubeScript)content.getScripts()
			.get(Math.toIntExact(bookmarkRequestDto.sentenceIndex())))
			.getStartTimeInSecond();
	}

	private String truncate(String content, int maxLength) {
		return content.length() > maxLength ? content.substring(0, maxLength) : content;
	}
}
