package com.biengual.userapi.content.domain.entity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.biengual.userapi.script.domain.entity.Script;
import com.biengual.userapi.util.MongoBaseDocument;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Document(collection = "content")
@Getter
@TypeAlias("content")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentDocument extends MongoBaseDocument {

	private List<Script> scripts;

	private List<String> questionIds;

	@Builder
	public ContentDocument(List<Script> scriptList) {
		this.scripts = scriptList;
		this.questionIds = new ArrayList<>();
	}

	public void updateScript(List<Script> scriptList) {
		this.scripts = scriptList;
	}

	public void updateQuestionIds(List<String> questionIds) {
		this.questionIds.addAll(questionIds);
	}
}