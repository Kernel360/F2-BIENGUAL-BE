package com.biengual.userapi.bookmark.application;

import static com.biengual.userapi.message.error.code.BookmarkErrorCode.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biengual.userapi.bookmark.domain.BookmarkCommand;
import com.biengual.userapi.bookmark.domain.BookmarkInfo;
import com.biengual.userapi.bookmark.domain.BookmarkReader;
import com.biengual.userapi.bookmark.domain.BookmarkService;
import com.biengual.userapi.bookmark.domain.BookmarkStore;
import com.biengual.userapi.message.error.exception.CommonException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {
	private final BookmarkReader bookmarkReader;
	private final BookmarkStore bookmarkStore;

	@Override
	@Transactional(readOnly = true)
	public BookmarkInfo.PositionInfo getBookmarks(BookmarkCommand.GetByContents command) {
		return BookmarkInfo.PositionInfo.of(bookmarkReader.getContentList(command));
	}

	@Override
	@Transactional(readOnly = true)
	public BookmarkInfo.MyListInfo getAllBookmarks(Long userId) {
		return BookmarkInfo.MyListInfo.of(bookmarkReader.getAllBookmarks(userId));
	}

	@Override
	@Transactional
	public void createBookmark(BookmarkCommand.Create command) {
		if (bookmarkReader.isBookmarkAlreadyPresent(command)) {
			throw new CommonException(BOOKMARK_ALREADY_EXISTS);
		}
		bookmarkStore.saveBookmark(command);
	}

	@Override
	@Transactional
	public BookmarkInfo.Position updateBookmark(BookmarkCommand.Update command) {
		return bookmarkStore.updateBookmark(command);
	}

	@Override
	@Transactional
	public void deleteBookmark(BookmarkCommand.Delete command) {
		bookmarkStore.deleteBookmark(command);
	}

}
