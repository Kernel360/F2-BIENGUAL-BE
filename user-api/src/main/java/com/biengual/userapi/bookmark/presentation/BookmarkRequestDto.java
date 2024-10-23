package com.biengual.userapi.bookmark.presentation;

import jakarta.validation.constraints.NotNull;

public class BookmarkRequestDto {

	public record CreateReq(
		@NotNull
		Long sentenceIndex,
		Long wordIndex,
		String description
	) {
	}

	public record UpdateReq(
		@NotNull
		Long bookmarkId,
		String description
	) {
	}

}
