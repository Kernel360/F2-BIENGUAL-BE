package com.biengual.userapi.category.domain.dto;

import com.biengual.userapi.category.domain.entity.CategoryEntity;

public record CategoryRequestDto(
	String name
) {
	public CategoryEntity toEntity() {
		return CategoryEntity.builder()
			.name(this.name)
			.build();
	}
}
