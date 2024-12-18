package com.biengual.userapi.crawling.application;

import static com.biengual.core.response.error.code.CrawlingErrorCode.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;

import com.biengual.core.annotation.ApiClient;
import com.biengual.core.response.error.exception.CommonException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
@ApiClient
public class TranslateApiClient {
	// Instantiates the OkHttpClient.
	OkHttpClient client = new OkHttpClient();

	@Value("${microsoft.key}")
	private String subscriptionKey;

	@Value("${microsoft.endpoint}")
	private String endpoint;

	@Value("${microsoft.location}")
	private String location;

	@Value("${spring.profiles.active}")
	private String activeProfile;

	public String translate(String text, String from, String to) {
		return extractText(Post(text, from, to));
	}

	// This function prettifies the json response.
	private String extractText(String jsonText) {
		JsonParser parser = new JsonParser();
		JsonElement json = parser.parse(jsonText);
		JsonArray translations = json.getAsJsonArray().get(0).getAsJsonObject().getAsJsonArray("translations");
		return translations.get(0).getAsJsonObject().get("text").getAsString();
	}

	// This function performs a POST request.
	private String Post(String text, String from, String to) {
		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body
			= RequestBody.create(mediaType, "[{\"Text\": \"" + escapeProblematicCharacters(text) + "\"}]");
		Request request = new Request.Builder()
			.url(endpoint + "&from=" + from + "&to=" + to)
			.post(body)
			.addHeader("Ocp-Apim-Subscription-Key", subscriptionKey)
			.addHeader("Ocp-Apim-Subscription-Region", location)
			.addHeader("Content-type", "application/json")
			.build();
		try {
			Response response = client.newCall(request).execute();
			if (response.code() != 200) { // Bad Request
				log.error("AZURE JSON TYPE ERROR : {}", text);
				return text;
			}
			return Objects.requireNonNull(response.body()).string();
		} catch (IOException | IllegalStateException e) {
			throw new CommonException(CRAWLING_TRANSLATE_FAILURE);
		}
	}

	private String escapeProblematicCharacters(String text) {
		Map<Character, String> replacements = new HashMap<>();
		replacements.put('\\', "\\\\");
		replacements.put('"', "\\\"");
		replacements.put('\n', "\\n");
		replacements.put('\r', "\\r");
		replacements.put('\t', "\\t");

		// OS 확인
		boolean isLinux = System.getProperty("os.name").toLowerCase().contains("linux");
		boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

		StringBuilder escapedText = new StringBuilder();

		String encodedText = text;
		if ("local".equals(activeProfile)) {
			encodedText = new String(text.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
		}

		for (char c : encodedText.toCharArray()) {
			if (replacements.containsKey(c)) {
				if (isLinux && c == '\n') {
					escapedText.append("\\n");
				} else if (isWindows && c == '\r') {
					escapedText.append("\\r");
				} else {
					escapedText.append(replacements.get(c));
				}
			} else {
				escapedText.append(c);
			}
		}

		return escapedText.toString();
	}

}