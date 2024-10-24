package com.biengual.userapi.scrap.domain;

import java.util.List;

public interface ScrapReader {
	List<ScrapInfo.View> findAllByUserId(Long userId);

	boolean existsScrap(ScrapCommand.GetByContents command);
}
