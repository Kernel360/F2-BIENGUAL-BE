package com.biengual.userapi.bookmark.domain.entity;

import com.biengual.userapi.bookmark.domain.dto.BookmarkRequestDto;
import com.biengual.userapi.user.domain.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "bookmark")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookmarkEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, columnDefinition = "bigint")
	private Long scriptIndex;

	@Column(columnDefinition = "bigint")
	private Long sentenceIndex;

	@Column(columnDefinition = "bigint")
	private Long wordIndex;

	@Column(columnDefinition = "varchar(512)")
	private String detail;

	@Column(columnDefinition = "varchar(255)")
	private String description;

	@Column(nullable = true, columnDefinition = "double")
	private Double startTimeInSecond;

	@Builder
	public BookmarkEntity(
		@NotNull Long scriptIndex, Long sentenceIndex, Long wordIndex,
		String detail, String description, Double startTimeInSecond
	) {
		this.scriptIndex = scriptIndex;
		this.sentenceIndex = sentenceIndex;
		this.wordIndex = wordIndex;
		this.detail = detail;
		this.description = description;
		this.startTimeInSecond = startTimeInSecond;
	}

	public void updateDescription(BookmarkRequestDto.BookmarkUpdateRequest bookmarkRequestDto) {
		this.description = bookmarkRequestDto.description();
	}
}
