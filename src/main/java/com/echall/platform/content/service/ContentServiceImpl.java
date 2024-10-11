package com.echall.platform.content.service;

import com.echall.platform.category.domain.entity.CategoryEntity;
import com.echall.platform.category.repository.CategoryRepository;
import com.echall.platform.content.domain.dto.ContentRequestDto;
import com.echall.platform.content.domain.dto.ContentResponseDto;
import com.echall.platform.content.domain.entity.ContentDocument;
import com.echall.platform.content.domain.entity.ContentEntity;
import com.echall.platform.content.domain.enums.ContentStatus;
import com.echall.platform.content.domain.enums.ContentType;
import com.echall.platform.content.repository.ContentRepository;
import com.echall.platform.content.repository.ContentScriptRepository;
import com.echall.platform.crawling.domain.dto.CrawlingResponseDto;
import com.echall.platform.crawling.service.CrawlingServiceImpl;
import com.echall.platform.message.error.exception.CommonException;
import com.echall.platform.util.PaginationDto;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.echall.platform.message.error.code.CategoryErrorCode.CATEGORY_NOT_FOUND;
import static com.echall.platform.message.error.code.ContentErrorCode.CONTENT_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {

	private final ContentRepository contentRepository;
	private final ContentScriptRepository contentScriptRepository;
	private final CrawlingServiceImpl crawlingService;
	private final CategoryRepository categoryRepository;

	@Override
	@Transactional(readOnly = true)
	public PaginationDto<ContentResponseDto.ContentPreviewResponseDto> getAllContents(
		ContentType contentType, Pageable pageable
	) {
		Page<ContentEntity> page = contentRepository.findByContentType(contentType, pageable);

		List<ContentResponseDto.ContentPreviewResponseDto> contents = page.getContent().stream()
			.map(ContentResponseDto.ContentPreviewResponseDto::from)
			.toList();

		return PaginationDto.from(page, contents);
	}

	@Override
	@Transactional
	public ContentResponseDto.ContentCreateResponseDto createContent(
		Authentication authentication,
		ContentRequestDto.ContentCreateRequestDto contentCreateRequestDto
	) throws Exception {
		CrawlingResponseDto.CrawlingContentResponseDto crawlingContentResponseDto = null;

		if (contentCreateRequestDto.contentType().equals(ContentType.LISTENING)) {
			crawlingContentResponseDto = crawlingService.getYoutubeInfo(
				contentCreateRequestDto.url(), String.valueOf(authentication.getCredentials())
			);
		}
		if (contentCreateRequestDto.contentType().equals(ContentType.READING)) {
			crawlingContentResponseDto = crawlingService.getCNNInfo(
				contentCreateRequestDto.url(), String.valueOf(authentication.getCredentials())
			);
		}

		ContentDocument contentDocument = crawlingContentResponseDto.toDocument();
		contentScriptRepository.save(contentDocument);

		CategoryEntity category = getCategoryEntity(crawlingContentResponseDto);

		ContentEntity content = crawlingContentResponseDto.toEntity(
			contentDocument.getId(), contentCreateRequestDto.contentType(), category
		);

		contentRepository.save(content);

		return new ContentResponseDto.ContentCreateResponseDto(contentDocument.getId().toString(), content.getId());
	}

	@Override
	@Transactional
	public ContentResponseDto.ContentUpdateResponseDto updateContent(
		Long id, ContentRequestDto.ContentUpdateRequestDto contentUpdateRequest
	) {
		ContentEntity content = contentRepository.findById(id)
			.orElseThrow(() -> new CommonException(CONTENT_NOT_FOUND));
		content.update(contentUpdateRequest);
		contentRepository.save(content);

		ContentDocument contentDocument
			= contentScriptRepository.findContentDocumentById(new ObjectId(content.getMongoContentId()))
				.orElseThrow(() -> new CommonException(CONTENT_NOT_FOUND));

		contentDocument.updateScript(contentUpdateRequest.script());
		contentScriptRepository.save(contentDocument);

		return new ContentResponseDto.ContentUpdateResponseDto(content.getId());
	}

	public List<ContentResponseDto.ContentPreviewResponseDto> getPreviewContents(
		ContentType contentType, String sortBy, int num
	) {
		return contentRepository.getPreviewContents(contentType, sortBy, num);
	}

	@Override
	@Transactional
	public ContentResponseDto.ContentUpdateResponseDto deactivateContent(Long id) {
		ContentEntity content = contentRepository.findById(id)
			.orElseThrow(() -> new CommonException(CONTENT_NOT_FOUND));
		content.updateStatus(ContentStatus.DEACTIVATED);
		contentRepository.save(content);

		return new ContentResponseDto.ContentUpdateResponseDto(content.getId());
	}

	@Override
	@Transactional	// hit 증가 로직 있어서 readOnly 생략
	public ContentResponseDto.ContentDetailResponseDto getScriptsOfContent(Long id) {
		ContentEntity content = contentRepository.findById(id)
			.orElseThrow(() -> new CommonException(CONTENT_NOT_FOUND));
		ContentDocument contentDocument = contentScriptRepository.findById(
			new ObjectId(content.getMongoContentId())
		).orElseThrow(() -> new CommonException(CONTENT_NOT_FOUND));

		contentRepository.updateHit(id); // TODO: 추후 레디스로 바꿀 예정

		return new ContentResponseDto.ContentDetailResponseDto(
			id,
			content.getContentType(),
			content.getCategory().getName(),
			content.getTitle(),
			content.getThumbnailUrl(),
			contentDocument.getScripts()
		);
	}

	// Internal Methods=================================================================================================

	private CategoryEntity getCategoryEntity(CrawlingResponseDto.CrawlingContentResponseDto dto) {
		if (categoryRepository.existsByName(dto.category())) {
			return categoryRepository.findByName(dto.category())
				.orElseThrow(() -> new CommonException(CATEGORY_NOT_FOUND));
		}

		return categoryRepository.save(dto.toCategoryEntity());
	}

}
