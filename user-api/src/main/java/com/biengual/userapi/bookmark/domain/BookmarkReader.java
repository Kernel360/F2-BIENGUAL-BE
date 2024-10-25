package com.biengual.userapi.bookmark.domain;

import java.util.List;

public interface BookmarkReader {
	List<BookmarkInfo.Position> getContentList(BookmarkCommand.GetByContents command);

	List<BookmarkInfo.MyList> getAllBookmarks(Long userId);

	boolean isBookmarkAlreadyPresent(BookmarkCommand.Create command);
}
