package com.biengual.userapi.content.domain;

import com.biengual.core.domain.document.content.ContentSearchDocument;
import com.biengual.core.domain.entity.content.QContentEntity;
import com.biengual.core.enums.ContentStatus;
import com.biengual.core.enums.ContentType;
import com.biengual.core.response.error.exception.CommonException;
import com.biengual.userapi.recommender.domain.RecommenderInfo;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.biengual.core.constant.RestrictionConstant.PERIOD_FOR_POINT_CONTENT_ACCESS;
import static com.biengual.core.domain.entity.content.QContentEntity.contentEntity;
import static com.biengual.core.domain.entity.paymenthistory.QPaymentContentHistoryEntity.paymentContentHistoryEntity;
import static com.biengual.core.domain.entity.scrap.QScrapEntity.scrapEntity;
import static com.biengual.core.response.error.code.ContentErrorCode.CONTENT_SORT_COL_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class ContentCustomRepository {
    private final JPAQueryFactory queryFactory;

    public ContentType findContentTypeById(Long contentId) {
        return queryFactory
            .select(contentEntity.contentType)
            .from(contentEntity)
            .where(contentEntity.id.eq(contentId))
            .fetchFirst();

    }

    public String findMongoIdByContentId(Long contentId) {
        return queryFactory
            .select(contentEntity.mongoContentId)
            .from(contentEntity)
            .where(contentEntity.contentStatus.eq(ContentStatus.ACTIVATED).and(contentEntity.id.eq(contentId)))
            .fetchOne();
    }

    public boolean existsByUrl(String url) {
        return queryFactory
            .from(contentEntity)
            .where(contentEntity.url.eq(url))
            .fetchFirst() != null;
    }

    public String findThumbnailUrlById(Long contentId) {
        return queryFactory
            .select(contentEntity.thumbnailUrl)
            .from(contentEntity)
            .where(contentEntity.id.eq(contentId))
            .fetchOne();
    }

    // 상세 조회에 따른 조회수 + 1 을 위하 쿼리
    public void increaseHitsByContentId(Long contentId) {
        queryFactory
            .update(contentEntity)
            .set(contentEntity.hits, contentEntity.hits.add(1))
            .where(contentEntity.id.eq(contentId))
            .execute();
    }

    // 스크랩을 많이 한 컨텐츠를 조회하기 위한 쿼리
    public List<ContentInfo.PreviewContent> findContentsByScrapCount(Integer size, Long userId) {
        // 정렬된 커버링 인덱스
        List<Long> contentIds = queryFactory
            .select(scrapEntity.content.id)
            .from(scrapEntity)
            .where(scrapEntity.content.contentStatus.eq(ContentStatus.ACTIVATED))
            .groupBy(scrapEntity.content.id)
            .orderBy(scrapEntity.id.count().desc())
            .limit(size)
            .fetch();

        // contentIds가 비어있는 경우 빈 리스트 반환
        if (contentIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<ContentInfo.PreviewContent> unalignedScrapPreview = queryFactory
            .select(
                Projections.constructor(
                    ContentInfo.PreviewContent.class,
                    contentEntity.id,
                    contentEntity.title,
                    contentEntity.s3Url,
                    contentEntity.contentType,
                    contentEntity.preScripts,
                    contentEntity.category.name,
                    contentEntity.hits,
                    contentEntity.videoDuration,
                    contentEntity.contentLevel,
                    getIsScrappedByUserId(userId),
                    getIsPointRequiredByUserIdAndContent(userId, contentEntity.id, contentEntity.createdAt)
                )
            )
            .from(contentEntity)
            .where(contentEntity.id.in(contentIds))
            .fetch();

        // TODO: DB에서 정렬된 채로 가져올 수 있는 방법이 있다면?
        // 정렬된 커버링 인덱스의 순서에 따라 정렬
        return contentIds.stream()
            .map(id -> unalignedScrapPreview.stream()
                .filter(content -> content.contentId().equals(id))
                .findFirst()
                .orElse(null))
            .filter(Objects::nonNull)
            .toList();
    }

    // 검색 조건에 맞는 컨텐츠를 조회하기 위한 쿼리
    public Page<ContentInfo.PreviewContent> findPreviewPageBySearch(Pageable pageable, String keyword, Long userId) {
        BooleanExpression predicate = getSearchPredicate(keyword);

        return findPreviewPage(pageable, predicate, userId);
    }

    public Page<ContentInfo.PreviewContent> findPreviewPageByElasticSearch(
        List<ContentSearchDocument> searchContents, Pageable pageable, Long userId
    ) {
        // 1. Elasticsearch 결과에서 ID 추출
        List<Long> ids = searchContents.stream()
            .map(content -> Long.parseLong(content.getId()))
            .toList();

        // 2. BooleanExpression 생성
        BooleanExpression predicate = getPredicateFromElasticsearchResults(ids);

        // 3. 기존 페이징 로직 호출
        return findSearchPreviewPage(pageable, predicate, userId, ids);
    }

    // 컨텐츠 프리뷰 페이지 조회하기 위한 쿼리
    public Page<ContentInfo.ViewContent> findViewPageByContentTypeAndCategoryId(
        Pageable pageable, ContentType contentType, Long categoryId, Long userId
    ) {
        BooleanExpression predicate = getViewPredicate(contentType, categoryId);

        List<OrderSpecifier<?>> orderSpecifiers = getOrderSpecifiers(pageable);

        return findViewPage(pageable, predicate, orderSpecifiers, userId);
    }

    // 컨텐츠 프리뷰 조회하기 위한 쿼리
    public List<ContentInfo.PreviewContent> findPreviewBySizeAndSortAndContentType(
        Integer size, String sort, ContentType contentType, Long userId
    ) {
        BooleanExpression predicate = getPreviewPredicate(contentType);

        List<OrderSpecifier<?>> orderSpecifiers = getOrderSpecifiers(sort);

        return findPreview(size, predicate, orderSpecifiers, userId);
    }

    // 어드민 컨텐츠 페이지네이션 조회를 위한 쿼리
    public Page<ContentInfo.Admin> findContentDetailForAdmin(
        Pageable pageable, ContentType contentType, Long categoryId
    ) {
        BooleanExpression predicate = getAdminViewPredicate(contentType, categoryId);

        List<OrderSpecifier<?>> orderSpecifiers = getOrderSpecifiers(pageable);

        return findAdminViewPage(pageable, predicate, orderSpecifiers);
    }

    // 컨텐츠 생성 날짜 조회를 위한 쿼리
    public LocalDateTime findCreatedAtOfContentById(Long contentId) {
        return queryFactory
            .select(contentEntity.createdAt)
            .from(contentEntity)
            .where(contentEntity.id.eq(contentId))
            .fetchOne();
    }

    // 입력 Category를 가지는 Content Id를 조회수 순으로 최대 limit 만큼 조회하는 쿼리
    public List<Long> findPopularContentIdsInCategoryIdsWithLimit(List<Long> categoryIds, int limit) {
        return queryFactory
            .select(contentEntity.id)
            .from(contentEntity)
            .where(
                contentEntity.category.id.in(categoryIds)
                    .and(contentEntity.contentStatus.eq(ContentStatus.ACTIVATED))
            )
            .orderBy(contentEntity.hits.desc())
            .limit(limit)
            .fetch();
    }

    // Content Id를 조회수 순으로 최대 limit 만큼 조회하는 쿼리
    public List<Long> findPopularContentIdsWithLimit(int limit) {
        return queryFactory
            .select(contentEntity.id)
            .from(contentEntity)
            .where(contentEntity.contentStatus.eq(ContentStatus.ACTIVATED))
            .orderBy(contentEntity.hits.desc())
            .limit(limit)
            .fetch();
    }

    // 추천된 Content 조회하는 쿼리
    public List<RecommenderInfo.Preview> findRecommendedContentsIn(Long userId, Set<Long> contentIds) {
        return queryFactory
            .select(
                Projections.constructor(
                    RecommenderInfo.Preview.class,
                    contentEntity.id,
                    contentEntity.title,
                    contentEntity.s3Url,
                    contentEntity.contentType,
                    contentEntity.category.name,
                    contentEntity.contentLevel,
                    getIsPointRequiredByUserIdAndContent(userId, contentEntity.id, contentEntity.createdAt)
                )
            )
            .from(contentEntity)
            .where(contentEntity.id.in(contentIds))
            .orderBy(contentEntity.id.desc())
            .fetch();
    }

    // Internal Method =================================================================================================

    // TODO: Predicate를 사용하지 않는 경우에는 Override? 아니면 null로 입력?
    // Preview Page를 위한 공통 Pagination 쿼리
    private Page<ContentInfo.PreviewContent> findPreviewPage(Pageable pageable, Predicate predicate, Long userId) {
        List<ContentInfo.PreviewContent> contents = queryFactory
            .select(
                Projections.constructor(
                    ContentInfo.PreviewContent.class,
                    contentEntity.id,
                    contentEntity.title,
                    contentEntity.s3Url,
                    contentEntity.contentType,
                    contentEntity.preScripts,
                    contentEntity.category.name,
                    contentEntity.hits,
                    contentEntity.videoDuration,
                    contentEntity.contentLevel,
                    getIsScrappedByUserId(userId),
                    getIsPointRequiredByUserIdAndContent(userId, contentEntity.id, contentEntity.createdAt)
                )
            )
            .from(contentEntity)
            .where(predicate)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = queryFactory
            .select(contentEntity.id.count())
            .from(contentEntity)
            .where(predicate);

        return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne);
    }

    private Page<ContentInfo.PreviewContent> findSearchPreviewPage(
        Pageable pageable, Predicate predicate, Long userId, List<Long> elasticIds
    ) {
        // Elasticsearch ID 순서에 따라 정렬 조건 생성
        OrderSpecifier<Integer> sortOrder = getElasticsearchSortOrder(elasticIds);

        // 데이터 조회
        List<ContentInfo.PreviewContent> contents = queryFactory
            .select(
                Projections.constructor(
                    ContentInfo.PreviewContent.class,
                    contentEntity.id,
                    contentEntity.title,
                    contentEntity.s3Url,
                    contentEntity.contentType,
                    contentEntity.preScripts,
                    contentEntity.category.name,
                    contentEntity.hits,
                    contentEntity.videoDuration,
                    contentEntity.contentLevel,
                    getIsScrappedByUserId(userId),
                    getIsPointRequiredByUserIdAndContent(userId, contentEntity.id, contentEntity.createdAt)
                )
            )
            .from(contentEntity)
            .where(predicate)
            .orderBy(sortOrder) // Elasticsearch ID 순서로 정렬
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        // 카운트 쿼리
        JPAQuery<Long> countQuery = queryFactory
            .select(contentEntity.id.count())
            .from(contentEntity)
            .where(predicate);

        return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne);
    }

    private OrderSpecifier<Integer> getElasticsearchSortOrder(List<Long> elasticIds) {
        // Elasticsearch ID 순서에 따라 정렬 조건 생성
        CaseBuilder.Cases<Integer, NumberExpression<Integer>> sortOrder = new CaseBuilder()
            .when(contentEntity.id.eq(elasticIds.get(0))).then(0);

        for (int i = 1; i < elasticIds.size(); i++) {
            sortOrder = sortOrder.when(contentEntity.id.eq(elasticIds.get(i))).then(i);
        }

        return sortOrder.otherwise(Integer.MAX_VALUE).asc(); // 나머지 항목은 마지막으로 정렬
    }

    // TODO: Predicate를 사용하지 않는 경우에는 Override? 아니면 null로 입력?
    // View Page를 위한 공통 Pagination 쿼리
    private Page<ContentInfo.ViewContent> findViewPage(
        Pageable pageable, Predicate predicate, List<OrderSpecifier<?>> orderSpecifiers, Long userId
    ) {
        List<ContentInfo.ViewContent> contents = queryFactory
            .select(
                Projections.constructor(
                    ContentInfo.ViewContent.class,
                    contentEntity.id,
                    contentEntity.title,
                    contentEntity.s3Url,
                    contentEntity.contentType,
                    contentEntity.preScripts,
                    contentEntity.category.name,
                    contentEntity.hits,
                    contentEntity.videoDuration,
                    contentEntity.contentLevel,
                    getIsScrappedByUserId(userId),
                    getIsPointRequiredByUserIdAndContent(userId, contentEntity.id, contentEntity.createdAt)
                )
            )
            .from(contentEntity)
            .where(predicate)
            .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = queryFactory
            .select(contentEntity.id.count())
            .from(contentEntity)
            .where(predicate);

        return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne);
    }

    // TODO: Predicate를 사용하지 않는 경우에는 Override? 아니면 null로 입력?
    // Preview를 위한 공통 쿼리
    private List<ContentInfo.PreviewContent> findPreview(
        Integer size, Predicate predicate, List<OrderSpecifier<?>> orderSpecifiers, Long userId
    ) {
        return queryFactory
            .select(
                Projections.constructor(
                    ContentInfo.PreviewContent.class,
                    contentEntity.id,
                    contentEntity.title,
                    contentEntity.s3Url,
                    contentEntity.contentType,
                    contentEntity.preScripts,
                    contentEntity.category.name,
                    contentEntity.hits,
                    contentEntity.videoDuration,
                    contentEntity.contentLevel,
                    getIsScrappedByUserId(userId),
                    getIsPointRequiredByUserIdAndContent(userId, contentEntity.id, contentEntity.createdAt)
                )
            )
            .from(contentEntity)
            .where(predicate)
            .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
            .limit(size)
            .fetch();
    }

    // Public API에서 사용하는 공통 Predicate
    private BooleanExpression getPublicContentsPredicate() {
        return contentEntity.contentStatus.eq(ContentStatus.ACTIVATED);
    }

    // keyword 검색 비지니스 로직
    private List<String> splitAndLimitWords(String keyword) {
        return Arrays.stream(keyword.strip().replace(',', ' ').split("\\s+"))
            .limit(10)
            .toList();
    }

    // 각 word에 대한 Predicate
    private BooleanExpression getWordPredicate(String word) {
        return contentEntity.title.containsIgnoreCase(word)
            .or(contentEntity.preScripts.containsIgnoreCase(word));
    }

    // 최종 검색 Predicate
    private BooleanExpression getSearchPredicate(String keyword) {
        BooleanExpression baseExpression = getPublicContentsPredicate();

        List<String> selectedSearchWords = splitAndLimitWords(keyword);

        BooleanExpression searchExpression = selectedSearchWords.stream()
            .map(this::getWordPredicate)
            .reduce(BooleanExpression::or)
            .orElse(null);

        return baseExpression.and(searchExpression);
    }

    // Elastic Search Predicate
    private BooleanExpression getPredicateFromElasticsearchResults(List<Long> ids) {
        // Elasticsearch 결과가 없으면 null 반환 (조건 없음)
        if (ids.isEmpty()) {
            return null;
        }

        // ID 리스트를 기반으로 BooleanExpression 생성
        BooleanExpression idPredicate = contentEntity.id.in(ids);

        // contentEntity.status가 ACTIVATED인 조건 추가
        BooleanExpression statusPredicate = contentEntity.contentStatus.eq(ContentStatus.ACTIVATED);

        // 두 조건 결합
        return idPredicate.and(statusPredicate);
    }

    // 컨텐츠 뷰 Predicate
    private BooleanExpression getViewPredicate(ContentType contentType, Long categoryId) {
        BooleanExpression baseExpression = getPublicContentsPredicate();

        BooleanExpression viewExpression = contentEntity.contentType.eq(contentType)
            .and(categoryId != null ? contentEntity.category.id.eq(categoryId) : null);

        return baseExpression.and(viewExpression);
    }

    // 어드민 컨텐츠 뷰 Predicate - 컨텐츠 뷰 Predicate 에서 ACTIVATED/DEACTIVATED 확인하는 부분 빠짐
    private BooleanExpression getAdminViewPredicate(
        ContentType contentType, Long categoryId
    ) {

        return contentEntity.contentType.eq(contentType)
            .and(categoryId != null ? contentEntity.category.id.eq(categoryId) : null);
    }

    // 컨텐츠 프리뷰 Predicate
    private BooleanExpression getPreviewPredicate(ContentType contentType) {
        BooleanExpression baseExpression = getPublicContentsPredicate();

        BooleanExpression previewExpression = contentEntity.contentType.eq(contentType);

        return baseExpression.and(previewExpression);
    }

    // TODO: 정렬 가능 필드 범위가 정해지면 Dto 단계에서 검증할 것
    // Pageable OrderSpecifiers
    private List<OrderSpecifier<?>> getOrderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        for (Sort.Order order : pageable.getSort()) {
            try {
                QContentEntity.class.getDeclaredField(order.getProperty());
            } catch (NoSuchFieldException e) {
                throw new CommonException(CONTENT_SORT_COL_NOT_FOUND);
            }

            PathBuilder pathBuilder = new PathBuilder(QContentEntity.class, "contentEntity");
            orderSpecifiers.add(new OrderSpecifier(
                order.isAscending() ? Order.ASC : Order.DESC,
                pathBuilder.get(order.getProperty())
            ));
        }

        return orderSpecifiers;
    }

    // TODO: 정렬 가능 필드 범위가 정해지면 Dto 단계에서 검증할 것
    // Field OrderSpecifiers
    private List<OrderSpecifier<?>> getOrderSpecifiers(String sort) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        Field field;
        try {
            field = contentEntity.getClass().getField(sort);
        } catch (NoSuchFieldException e) {
            throw new CommonException(CONTENT_SORT_COL_NOT_FOUND);
        }

        Path<Object> path = Expressions.path(Object.class, contentEntity, field.getName());

        orderSpecifiers.add(new OrderSpecifier(Order.DESC, path));

        return orderSpecifiers;
    }

    // Admin view를 위한 공통 쿼리
    private Page<ContentInfo.Admin> findAdminViewPage(
        Pageable pageable, BooleanExpression predicate, List<OrderSpecifier<?>> orderSpecifiers
    ) {
        List<ContentInfo.Admin> contents = queryFactory
            .select(
                Projections.constructor(
                    ContentInfo.Admin.class,
                    contentEntity.id,
                    contentEntity.title,
                    contentEntity.category.name,
                    contentEntity.contentType,
                    contentEntity.hits,
                    contentEntity.numOfQuiz,
                    contentEntity.contentStatus
                )
            )
            .from(contentEntity)
            .where(predicate)
            .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = queryFactory
            .select(contentEntity.id.count())
            .from(contentEntity)
            .where(predicate);

        return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne);
    }

    // isScrapped를 col로 받기 위한 확인하는 쿼리, 비로그인 상태 시 false 리턴
    private Expression<?> getIsScrappedByUserId(Long userId) {
        return userId != null ?
            JPAExpressions
                .selectOne()
                .from(scrapEntity)
                .where(scrapEntity.content.id.eq(contentEntity.id)
                    .and(scrapEntity.userId.eq(userId)))
                .exists()
            : Expressions.constant(false);
    }

    // isPointRequired를 col로 받기 위해 확인하는 쿼리, 비로그인 상태 true 리턴
    private Expression<?> getIsPointRequiredByUserIdAndContent(
        Long userId, NumberPath<Long> contentId, DateTimePath<LocalDateTime> createdAt
    ) {
        // 오늘 날짜 기준으로 7일 전 날짜 계산
        LocalDate fewDaysAgo = LocalDate.now().minusDays(PERIOD_FOR_POINT_CONTENT_ACCESS);
        // createdAt이 7일 이내인지 확인
        BooleanExpression isWithinFewDays =
            Expressions.dateTemplate(LocalDate.class, "date({0})", createdAt).goe(fewDaysAgo);

        if (userId == null) {
            return isWithinFewDays;
        }

        return JPAExpressions
            .select(paymentContentHistoryEntity)
            .from(paymentContentHistoryEntity)
            .where(
                paymentContentHistoryEntity.contentId.eq(contentId)
                    .and(paymentContentHistoryEntity.userId.eq(userId))
            )
            .notExists()
            .and(isWithinFewDays); // createdAt 기준 7일 확인
    }
}
