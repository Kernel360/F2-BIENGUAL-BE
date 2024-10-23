package com.biengual.userapi.bookmark.application;

import org.springframework.stereotype.Component;

import com.biengual.userapi.bookmark.domain.BookmarkCommand;
import com.biengual.userapi.bookmark.domain.BookmarkInfo;
import com.biengual.userapi.bookmark.domain.BookmarkService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BookmarkFacade {
	private final BookmarkService bookmarkService;

	public BookmarkInfo.ContentInfo getBookmarks(BookmarkCommand.GetByContents command) {
		return bookmarkService.getBookmarks(command);
	}

	public BookmarkInfo.MyListInfo getAllBookmarks(Long userId) {
		return bookmarkService.getAllBookmarks(userId);
	}

	public BookmarkInfo.Create createBookmark(BookmarkCommand.Create command){
		return bookmarkService.createBookmark(command);
	}

	public BookmarkInfo.Content updateBookmark(BookmarkCommand.Update command) {
		return bookmarkService.updateBookmark(command);
	}

	public void deleteBookmark(BookmarkCommand.Delete command) {
		bookmarkService.deleteBookmark(command.userId(), command.bookmarkId());
	}
}
