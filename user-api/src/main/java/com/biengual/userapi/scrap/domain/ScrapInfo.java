package com.biengual.userapi.scrap.domain;

import java.time.LocalDateTime;
import java.util.List;

import com.biengual.core.enums.ContentLevel;
import com.biengual.core.enums.ContentStatus;
import com.biengual.core.enums.ContentType;

import lombok.Builder;

public class ScrapInfo {

	@Builder
	public record View(
		Long scrapId,
		Long contentId,
		String title,
		ContentType contentType,
		LocalDateTime createdAt,
		String preScripts,
		String thumbnailUrl,
		String category,
		Integer videoDurationInSeconds,
		ContentLevel calculatedLevel,
		ContentStatus contentStatus
	) {
	}

	@Builder
	public record ViewInfo(
		List<View> scrapList
	) {
		public static ViewInfo of(List<View> viewListInfo) {
			return ViewInfo.builder()
				.scrapList(viewListInfo)
				.build();
		}
	}
}
