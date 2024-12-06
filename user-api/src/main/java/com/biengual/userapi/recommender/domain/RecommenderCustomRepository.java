package com.biengual.userapi.recommender.domain;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.biengual.core.domain.entity.recommender.QBookmarkRecommenderEntity.bookmarkRecommenderEntity;

@Repository
@RequiredArgsConstructor
public class RecommenderCustomRepository {
    private final JPAQueryFactory queryFactory;

    // 이번주 인기 북마크 조회
    public List<RecommenderInfo.PopularBookmark> findPopularBookmarks(
        LocalDateTime startOfWeek, LocalDateTime endOfWeek
    ) {
        return queryFactory
            .select(
                Projections.constructor(
                    RecommenderInfo.PopularBookmark.class,
                    bookmarkRecommenderEntity.enDetail,
                    bookmarkRecommenderEntity.koDetail,
                    bookmarkRecommenderEntity.contentId
                )
            )
            .from(bookmarkRecommenderEntity)
            .where(
                bookmarkRecommenderEntity.startOfWeek.eq(startOfWeek)
                    .and(bookmarkRecommenderEntity.endOfWeek.eq(endOfWeek))
            )
            .fetch();
    }
}
