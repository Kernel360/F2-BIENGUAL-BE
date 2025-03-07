package com.biengual.userapi.question.domain;

import java.util.List;

import com.biengual.core.domain.document.question.QuestionDocument;
import com.biengual.core.enums.QuestionType;

import lombok.Builder;

public class QuestionInfo {

    @Builder
    public record Detail(
        String question,
        String id,
        List<String> examples,
        QuestionType type
    ) {
    }

    @Builder
    public record DetailInfo(
        List<Detail> questionAnswer
    ) {
        public static DetailInfo of(List<Detail> details) {
            return DetailInfo.builder()
                .questionAnswer(details)
                .build();
        }
    }

    @Builder
    public record Hint(
        String hint
    ) {
        public static Hint of(String hint) {
            return QuestionInfo.Hint.builder()
                .hint(hint)
                .build();
        }
    }
}
