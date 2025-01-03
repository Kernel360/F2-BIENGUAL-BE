package com.biengual.userapi.content.domain;

import static com.biengual.core.response.error.code.SearchContentErrorCode.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.FuzzyQuery;
import org.opensearch.client.opensearch._types.query_dsl.NestedQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.TermQuery;
import org.opensearch.client.opensearch.core.DeleteRequest;
import org.opensearch.client.opensearch.core.DeleteResponse;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.IndexResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.biengual.core.domain.document.content.ContentSearchDocument;
import com.biengual.core.response.error.exception.CommonException;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ContentSearchRepositoryImpl implements ContentSearchRepository {
    private final OpenSearchClient openSearchClient;

    @Value("${opensearch.index}")
    private String index;

    /**
     * 키워드로 콘텐츠를 검색합니다.
     *
     * @param keyword 검색 키워드
     * @return 검색 결과 리스트 (List<ContentSearchDocument>)
     */
    @Override
    public List<ContentSearchDocument> searchByFields(String keyword) {
        // Fuzzy Query 생성 (제목 검색)
        Query fuzzyQuery = new FuzzyQuery.Builder()
            .field("title")
            .value(FieldValue.of(keyword))
            .fuzziness("AUTO")
            .build()
            .toQuery();

        // Nested Query 생성 (스크립트 검색)
        Query nestedQuery = new NestedQuery.Builder()
            .path("scripts")
            .query(q -> q
                .bool(b -> b
                    .should(
                        new FuzzyQuery.Builder()
                            .field("scripts.enScript")
                            .value(FieldValue.of(keyword))
                            .fuzziness("AUTO")
                            .build()
                            .toQuery(),
                        new FuzzyQuery.Builder()
                            .field("scripts.koScript")
                            .value(FieldValue.of(keyword))
                            .fuzziness("AUTO")
                            .build()
                            .toQuery()
                    )
                )
            )
            .build()
            .toQuery();

        // term Query 생성 (카테고리 정확히 매칭)
        Query termQuery = new TermQuery.Builder()
            .field("categoryName")
            .value(FieldValue.of(keyword))
            .caseInsensitive(true)
            .build()
            .toQuery();

        // bool Query 생성 (조건 결합)
        Query boolQuery = new BoolQuery.Builder()
            .should(
                termQuery,
                fuzzyQuery,
                nestedQuery
            )
            .minimumShouldMatch("1")
            .build()
            .toQuery();

        // SearchRequest 생성
        SearchRequest searchRequest = new SearchRequest.Builder()
            .index(index)           // 인덱스 이름 설정
            .query(boolQuery)       // bool 쿼리 추가
            .timeout("10s")   // OpenSearch 과부하 방지 타임 아웃
            .build();

        // 요청 실행 및 응답 처리
        SearchResponse<ContentSearchDocument> response;
        try {
            response = openSearchClient.search(searchRequest, ContentSearchDocument.class);
        } catch (IOException e) {
            throw new CommonException(OPEN_SEARCH_FAILED);
        }

        // 결과 처리 및 반환
        List<ContentSearchDocument> results = new ArrayList<>();
        for (Hit<ContentSearchDocument> hit : response.hits().hits()) {
            results.add(hit.source());
        }
        return results;
    }

    /**
     * 콘텐츠를 OpenSearch에 저장합니다.
     *
     * @param document 저장할 ContentSearchDocument 객체
     */
    @Override
    public void saveContent(ContentSearchDocument document) {
        try {
            // IndexRequest 생성
            IndexRequest<ContentSearchDocument> indexRequest = new IndexRequest.Builder<ContentSearchDocument>()
                .index(index) // 인덱스 이름 설정
                .id(document.getId())  // 문서 ID 설정
                .document(document)    // 저장할 문서 설정
                .build();

            // 요청 실행
            IndexResponse response = openSearchClient.index(indexRequest);

            // 응답 확인 (필요시 로깅 추가)
            if (!response.result().name().equalsIgnoreCase("created") &&
                !response.result().name().equalsIgnoreCase("updated")) {
                throw new CommonException(SEARCH_CONTENT_SAVE_FAILED);
            }
        } catch (IOException e) {
            throw new CommonException(SEARCH_CONTENT_SAVE_FAILED);
        }
    }

    /**
     * 콘텐츠를 OpenSearch에서 삭제합니다.
     *
     * @param id 삭제할 문서의 ID
     */
    @Override
    public void deleteContent(String id) {
        try {
            // DeleteRequest 생성
            DeleteRequest deleteRequest = new DeleteRequest.Builder()
                .index(index) // 인덱스 이름 설정
                .id(id)        // 삭제할 문서 ID 설정
                .build();

            // 요청 실행
            DeleteResponse response = openSearchClient.delete(deleteRequest);

            // 응답 결과 처리
            if (!response.result().name().equalsIgnoreCase("deleted") &&
                !response.result().name().equalsIgnoreCase("not_found")) {
                throw new CommonException(SEARCH_CONTENT_DELETE_FAILED);
            }
        } catch (IOException e) {
            throw new CommonException(SEARCH_CONTENT_DELETE_FAILED);
        }
    }
}