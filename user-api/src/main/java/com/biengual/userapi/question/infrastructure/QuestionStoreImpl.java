package com.biengual.userapi.question.infrastructure;

import static com.biengual.userapi.message.error.code.ContentErrorCode.*;
import static com.biengual.userapi.question.domain.QuestionDocument.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bson.types.ObjectId;

import com.biengual.userapi.annotation.DataProvider;
import com.biengual.userapi.content.domain.entity.ContentDocument;
import com.biengual.userapi.content.domain.entity.ContentEntity;
import com.biengual.userapi.content.domain.enums.ContentStatus;
import com.biengual.userapi.content.repository.ContentRepository;
import com.biengual.userapi.content.repository.ContentScriptRepository;
import com.biengual.userapi.message.error.exception.CommonException;
import com.biengual.userapi.question.domain.QuestionCommand;
import com.biengual.userapi.question.domain.QuestionDocument;
import com.biengual.userapi.question.domain.QuestionRepository;
import com.biengual.userapi.question.domain.QuestionStore;
import com.biengual.userapi.question.domain.QuestionType;

import lombok.RequiredArgsConstructor;

@DataProvider
@RequiredArgsConstructor
public class QuestionStoreImpl implements QuestionStore {
	private final QuestionRepository questionRepository;
	private final ContentRepository contentRepository;
	private final ContentScriptRepository contentScriptRepository;

	@Override
	public void createQuestion(QuestionCommand.Create command) {
		Random random = new Random();
		Set<Integer> randomIdxes = new HashSet<>();
		List<String> questionIds = new ArrayList<>();
		List<String> randomScripts = new ArrayList<>();
		List<String> randomKoScripts = new ArrayList<>();

		ContentDocument contentDocument = this.getContentDocument(command.contentId());

		while (randomIdxes.size() < command.questionNumOfBlank() + command.questionNumOfOrder()) {
			randomIdxes.add(random.nextInt(contentDocument.getScripts().size()));
		}

		for (Integer idx : randomIdxes) {
			randomScripts.add(contentDocument.getScripts().get(idx).getEnScript());
			randomKoScripts.add(contentDocument.getScripts().get(idx).getKoScript());
		}

		int start = 0;
		// make blank question
		questionIds.addAll(makeBlankQuestion(randomScripts, randomKoScripts, command.questionNumOfBlank()));
		start += command.questionNumOfBlank();

		// make word order question
		questionIds.addAll(
			makeWordOrderQuestion(randomScripts, randomKoScripts, start, command.questionNumOfOrder()));

		// update QuestionIds
		contentDocument.updateQuestionIds(questionIds);
		contentScriptRepository.save(contentDocument);
	}

	// Internal Methods ------------------------------------------------------------------------------------------------
	private List<String> makeBlankQuestion(
		List<String> randomScripts, List<String> randomKoScripts, Integer numOfBlank
	) {
		List<String> questionIds = new ArrayList<>();
		Random random = new Random();

		for (int i = 0; i < numOfBlank; ++i) {
			String script = randomScripts.get(i);
			String[] words = script.split("\\s+");
			if (words.length > 1) {
				int blankIndex = random.nextInt(words.length);
				StringBuilder question = new StringBuilder();
				for (int j = 0; j < words.length; j++) {
					if (j == blankIndex) {
						question.append("____");
					} else {
						question.append(words[j]);
					}
					if (j < words.length - 1) {
						question.append(" ");
					}
				}
				questionIds.add(
					saveToMongo(
						question.toString(), randomKoScripts.get(i), words[blankIndex], QuestionType.BLANK
					)
				);
			}
		}
		return questionIds;
	}

	private List<String> makeWordOrderQuestion(
		List<String> randomScripts, List<String> randomKoScripts, Integer start, Integer numOfOrder
	) {
		List<String> questionIds = new ArrayList<>();
		Random random = new Random();

		for (int i = start; i < start + numOfOrder; ++i) {
			String script = randomScripts.get(i);
			String[] words = script.split("\\s+");
			if (words.length > 1) {
				List<String> shuffledWords = new ArrayList<>(Arrays.asList(words));
				Collections.shuffle(shuffledWords, random);
				String question = String.join(" ", shuffledWords);

				questionIds.add(
					saveToMongo(question, randomKoScripts.get(i), script, QuestionType.ORDER)
				);
			}
		}
		return questionIds;
	}

	private String saveToMongo(String question, String questionKo, String word, QuestionType type) {

		QuestionDocument questionDocument = of(question, questionKo, word, type);
		questionRepository.save(questionDocument);

		return questionDocument.getId().toString();
	}

	private ContentDocument getContentDocument(Long contentId) {
		ContentEntity content = contentRepository.findById(contentId)
			.orElseThrow(() -> new CommonException(CONTENT_NOT_FOUND));
		content.updateStatus(ContentStatus.ACTIVATED);

		return contentScriptRepository.findById(new ObjectId(content.getMongoContentId()))
			.orElseThrow(() -> new CommonException(CONTENT_NOT_FOUND));
	}
}
