package com.biengual.userapi.scrap.presentation;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.biengual.userapi.content.domain.entity.ContentEntity;
import com.biengual.userapi.oauth2.domain.info.OAuth2UserPrincipal;
import com.biengual.userapi.scrap.domain.ScrapCommand;
import com.biengual.userapi.scrap.domain.ScrapEntity;
import com.biengual.userapi.scrap.domain.ScrapInfo;

/**
 * do~ : Command <- Request
 * of~ : Response <- Info
 * build~ :  Entity <-> Info, Info <-> Info
 * <p>
 * ScrapDto 와 Info, Command 간의 Mapper
 *
 * @author 김영래
 */
@Mapper(
	componentModel = "spring",
	injectionStrategy = InjectionStrategy.CONSTRUCTOR,
	unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ScrapDtoMapper {

	// Command <- Request
	@Mapping(target = "userId", source = "principal.id")
	ScrapCommand.GetByContents doGetByContents(Long contentId, OAuth2UserPrincipal principal);

	@Mapping(target = "userId", source = "principal.id")
	ScrapCommand.Create doCreate(Long contentId, OAuth2UserPrincipal principal);

	@Mapping(target = "userId", source = "principal.id")
	ScrapCommand.Delete doDelete(Long contentId, OAuth2UserPrincipal principal);

	// Response <- Info
	ScrapResponseDto.ViewListRes ofViewRes(ScrapInfo.ViewInfo allScraps);

	// Entity <-> Info, Info <-> Info
	@Mapping(target = "scrapId", source = "id")
	@Mapping(target = "contentId", source = "content.id")
	@Mapping(target = "title", source = "content.title")
	@Mapping(target = "contentType", source = "content.contentType")
	@Mapping(target = "preScripts", source = "content.preScripts")
	@Mapping(target = "thumbnailUrl", source = "content.thumbnailUrl")
	ScrapInfo.View buildView(ScrapEntity scrap);

	@Mapping(target = "content", source = "content")
	ScrapEntity buildEntity(ContentEntity content);
}
