package com.biengual.userapi.bookmark.infrastructure;

import static com.biengual.userapi.message.error.code.UserErrorCode.*;

import java.util.Comparator;
import java.util.List;

import com.biengual.userapi.annotation.DataProvider;
import com.biengual.userapi.bookmark.domain.BookmarkCommand;
import com.biengual.userapi.bookmark.domain.BookmarkEntity;
import com.biengual.userapi.bookmark.domain.BookmarkInfo;
import com.biengual.userapi.bookmark.domain.BookmarkReader;
import com.biengual.userapi.bookmark.domain.BookmarkRepository;
import com.biengual.userapi.bookmark.presentation.BookmarkDtoMapper;
import com.biengual.userapi.content.repository.ContentRepository;
import com.biengual.userapi.message.error.exception.CommonException;
import com.biengual.userapi.user.domain.UserEntity;
import com.biengual.userapi.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@DataProvider
@RequiredArgsConstructor
public class BookmarkReaderImpl implements BookmarkReader {
	private final UserRepository userRepository;
	private final BookmarkRepository bookmarkRepository;
	private final ContentRepository contentRepository;
	private final BookmarkDtoMapper bookmarkDtoMapper;

	@Override
	public List<BookmarkInfo.Position> getContentList(BookmarkCommand.GetByContents command) {
		// TODO: queryDsl 로 한번에 작업
		UserEntity user = userRepository.findById(command.userId())
			.orElseThrow(() -> new CommonException(USER_NOT_FOUND));

		return user.getBookmarks()
			.stream()
			.filter(bookmarkEntity -> bookmarkEntity.getScriptIndex().equals(command.contentId()))
			.sorted(Comparator.comparing(BookmarkEntity::getUpdatedAt).reversed())
			.map(bookmarkDtoMapper::buildPosition)
			.toList();
	}

	@Override
	public List<BookmarkInfo.MyList> getAllBookmarks(Long userId) {
		List<BookmarkEntity> bookmarks = bookmarkRepository.getAllBookmarks(userId);
		return bookmarks.stream()
			.map(bookmark -> bookmarkDtoMapper.buildMyList(
				bookmark,
				contentRepository.findContentTypeById(bookmark.getScriptIndex()),
				contentRepository.findTitleById(bookmark.getScriptIndex())
			)).toList();
	}

	@Override
	public boolean isBookmarkAlreadyPresent(BookmarkCommand.Create command) {
		return bookmarkRepository.isBookmarkAlreadyPresent(command);
	}
}
