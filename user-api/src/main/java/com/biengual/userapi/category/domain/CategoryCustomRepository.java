package com.biengual.userapi.category.domain;

import static com.biengual.core.domain.entity.category.QCategoryEntity.*;
import static com.biengual.core.domain.entity.content.QContentEntity.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.biengual.core.enums.ContentType;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CategoryCustomRepository {
    private final JPAQueryFactory queryFactory;

    // 서비스에 등록된 모든 카테고리를 조회하기 위한 쿼리
    public List<CategoryInfo.Category> findAllCategories() {
        return queryFactory.select(
                Projections.constructor(
                    CategoryInfo.Category.class,
                    categoryEntity.id,
                    categoryEntity.name
                )
            )
            .from(categoryEntity)
            .fetch();
    }

    // ContentType 별로 카테고리 목록을 조회하기 위한 쿼리
    public List<CategoryInfo.Category> findCategoriesByContentType(ContentType contentType) {
        return queryFactory
            .select(
                Projections.constructor(
                    CategoryInfo.Category.class,
                    categoryEntity.id,
                    categoryEntity.name
                )
            )
            .from(categoryEntity)
            .join(contentEntity).on(contentEntity.category.eq(categoryEntity))
            .where(contentEntity.contentType.eq(contentType))
            .distinct()
            .fetch();
    }
}
