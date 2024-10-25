package com.biengual.userapi.question.infrastructure;

import static com.biengual.userapi.message.error.code.ContentErrorCode.*;
import static com.biengual.userapi.message.error.code.QuestionErrorCode.*;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.biengual.userapi.annotation.DataProvider;
import com.biengual.userapi.content.domain.entity.ContentDocument;
import com.biengual.userapi.content.domain.entity.ContentEntity;
import com.biengual.userapi.content.domain.enums.ContentStatus;
import com.biengual.userapi.content.repository.ContentRepository;
import com.biengual.userapi.content.repository.ContentScriptRepository;
import com.biengual.userapi.message.error.exception.CommonException;
import com.biengual.userapi.question.domain.QuestionDocument;
import com.biengual.userapi.question.domain.QuestionInfo;
import com.biengual.userapi.question.domain.QuestionReader;
import com.biengual.userapi.question.domain.QuestionRepository;

import lombok.RequiredArgsConstructor;

@DataProvider
@RequiredArgsConstructor
public class QuestionReaderImpl implements QuestionReader {
	private final QuestionRepository questionRepository;
	private final ContentRepository contentRepository;
	private final ContentScriptRepository contentScriptRepository;

	@Override
	public List<QuestionInfo.Detail> getQuestions(Long contentId) {

		ContentDocument contentDocument = this.getContentDocument(contentId);
		List<String> questionDocumentIds = contentDocument.getQuestionIds();
		List<QuestionInfo.Detail> questions = new ArrayList<>();

		for (String questionDocumentId : questionDocumentIds) {
			QuestionDocument questionDocument = questionRepository.findById(new ObjectId(questionDocumentId))
				.orElseThrow(() -> new CommonException(QUESTION_NOT_FOUND));
			questions.add(
				QuestionInfo.Detail.builder()
					.question(questionDocument.getQuestion())
					.questionKo(questionDocument.getQuestionKo())
					.answer(questionDocument.getAnswer())
					.type(questionDocument.getType())
					.build()
			);
		}
		return questions;
	}

	// Internal Methods ------------------------------------------------------------------------------------------------
	private ContentDocument getContentDocument(Long contentId) {
		ContentEntity content = contentRepository.findById(contentId)
			.orElseThrow(() -> new CommonException(CONTENT_NOT_FOUND));
		content.updateStatus(ContentStatus.ACTIVATED);

		return contentScriptRepository.findById(new ObjectId(content.getMongoContentId()))
			.orElseThrow(() -> new CommonException(CONTENT_NOT_FOUND));
	}
}
