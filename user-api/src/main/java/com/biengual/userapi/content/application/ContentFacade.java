package com.biengual.userapi.content.application;

import com.biengual.userapi.annotation.Facade;
import com.biengual.userapi.content.domain.ContentCommand;
import com.biengual.userapi.content.domain.ContentInfo;
import com.biengual.userapi.content.domain.ContentService;
import com.biengual.userapi.crawling.domain.CrawlingService;
import lombok.RequiredArgsConstructor;

@Facade
@RequiredArgsConstructor
public class ContentFacade {
	private final CrawlingService crawlingService;
	private final ContentService contentService;

	public void createContent(ContentCommand.CrawlingContent crawlingContentCommand) {
		ContentCommand.Create createContent = crawlingService.getCrawlingDetail(crawlingContentCommand);
		contentService.createContent(createContent);
	}

	public void modifyContent(ContentCommand.Modify command) {
		contentService.updateContent(command);
	}

	public void deactivateContent(Long id) {
		contentService.deactivateContent(id);
	}

    // 스크랩 많은 순 컨텐츠 조회
    public ContentInfo.PreviewContents getContentsByScrapCount(Integer size) {
        return contentService.getContentsByScrapCount(size);
    }
}
