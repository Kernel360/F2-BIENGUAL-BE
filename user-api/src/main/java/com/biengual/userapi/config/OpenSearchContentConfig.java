package com.biengual.userapi.config;

import static com.biengual.core.constant.ServiceConstant.*;
import static com.biengual.core.response.error.code.SearchContentErrorCode.*;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.Time;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.IndexSettings;
import org.springframework.context.annotation.Configuration;

import com.biengual.core.response.error.exception.CommonException;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class OpenSearchContentConfig {
    private final OpenSearchClient openSearchClient;

    /**
     * 인덱스가 없을 경우 인덱스 초기화
     */
    @PostConstruct
    public void init() {
        this.createIndexIfNotExists(openSearchClient);
    }

    private void createIndexIfNotExists(OpenSearchClient client) {
        try {
            boolean exists = client.indices().exists(b -> b.index(CONTENT_SEARCH_INDEX_NAME)).value();
            if (!exists) {
                // Define index settings
                // TODO: 성능 부족하면 샤드, 레플리카 추가해야 하는지 고려
                IndexSettings settings = new IndexSettings.Builder()
                    .numberOfShards("1") // Set number of shards
                    .numberOfReplicas("0") // Set number of replicas
                    .refreshInterval(new Time.Builder().time("30s").build())
                    .analysis(a -> a
                        .analyzer("nori_analyzer", na -> na
                            .custom(c -> c
                                .tokenizer("nori_tokenizer")
                                .filter("nori_part_of_speech", "lowercase", "stop")
                            )
                        )
                        .analyzer("custom_analyzer", ca -> ca
                            .custom(c -> c
                                .tokenizer("standard")
                                .filter("lowercase", "stop", "asciifolding")
                            )
                        )
                    )
                    .build();

                // Define index mappings
                TypeMapping mappings = new TypeMapping.Builder()
                    .properties("id", p -> p.keyword(k -> k.index(false)))
                    .properties("title", p -> p.text(t -> t.analyzer("custom_analyzer")))
                    .properties("categoryName", p -> p.keyword(k -> k.normalizer("lowercase")))
                    .properties("scripts", p -> p.nested(n -> n
                        .properties("enScript", sp -> sp.text(t -> t.analyzer("custom_analyzer")))
                        .properties("koScript", sp -> sp.text(t -> t.analyzer("nori_analyzer")))
                    ))
                    .build();

                // Create the index with settings and mappings
                CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder()
                    .index(CONTENT_SEARCH_INDEX_NAME)
                    .settings(settings)
                    .mappings(mappings)
                    .build();

                client.indices().create(createIndexRequest);
            }
        } catch (Exception e) {
            throw new CommonException(SEARCH_CONTENT_SAVE_FAILED);
        }
    }
}
