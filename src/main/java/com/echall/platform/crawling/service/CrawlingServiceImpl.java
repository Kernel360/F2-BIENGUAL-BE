package com.echall.platform.crawling.service;

import static com.echall.platform.message.error.code.CrawlingErrorCode.*;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.echall.platform.crawling.domain.dto.CrawlingResponseDto;
import com.echall.platform.message.error.exception.CommonException;
import com.echall.platform.script.domain.entity.CNNScript;
import com.echall.platform.script.domain.entity.Script;
import com.echall.platform.script.domain.entity.YoutubeScript;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlingServiceImpl implements CrawlingService {
	private final TranslateService translateService;
	@Value("${YOUTUBE_API_KEY}")
	private String YOUTUBE_API_KEY;

	@Override
	public CrawlingResponseDto.CrawlingContentResponseDto getYoutubeInfo(String youtubeUrl, String credentials)
		throws Exception {
		// Extract the video ID from the URL
		String videoId = extractVideoId(youtubeUrl);
		// Create the request URL for the transcript service
		String requestUrl = "https://www.googleapis.com/youtube/v3/videos?id=" + videoId
			+ "&part=snippet, contentDetails" + "&key=" + YOUTUBE_API_KEY;

		// Set up headers with your API key
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cookie", "access_token=" + credentials);

		// Create an HTTP entity with headers
		// Use RestTemplate to make the request
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response
			= restTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);

		JsonNode snippetNode = getSnippetNode(response.getBody()).path("snippet");
		JsonNode contentDetailsNode = getSnippetNode(response.getBody()).path("contentDetails");

		Duration duration = Duration.parse(contentDetailsNode.path("duration").asText());
		if (duration.compareTo(Duration.ofMinutes(8)) > 0) {
			throw new CommonException(CRAWLING_OUT_OF_BOUNDS);
		}

		return new CrawlingResponseDto.CrawlingContentResponseDto(
			youtubeUrl,
			snippetNode.path("title").asText(),
			getThumbnailUrl(snippetNode.path("thumbnails")),
			getCategoryName(snippetNode.path("categoryId").asText()),
			getYoutubeScript(youtubeUrl, Double.parseDouble(String.valueOf(duration.getSeconds())))
		);
	}

	@Override
	public CrawlingResponseDto.CrawlingContentResponseDto getCNNInfo(String cnnUrl, String credentials) {
		CrawlingResponseDto.CrawlingContentResponseDto cnnResponseDto = null;
		try {
			cnnResponseDto = fetchArticle(cnnUrl);
		} catch (IOException e) {
			throw new CommonException(CRAWLING_JSOUP_FAILURE);
		}

		return cnnResponseDto;
	}

	/**
	 * 이 기사 크롤링 과정에서 특정 요소들이 일부 빠져 있어도 크롤링 자체는 되고 있지만 원하는 요소들이 빠지게 되면 다른 컨텐츠들에 비해
	 * 다소 아쉬운 부분이 보여서 이런 부분을 체크하는 로직을 할 지 혹은 입력하는 사람이 제대로 확인하고 사용하는 현재 방식을 유지할지
	 * 고민중입니다
	 */
	@Override
	public CrawlingResponseDto.CrawlingContentResponseDto fetchArticle(String url) throws IOException {
		Document doc = Jsoup.connect(url).get();

		// 제목 추출
		String title = doc.select("h1.headline__text").text();

		// 카테고리 추출
		Element categoryElement = doc.selectFirst("meta[name=meta-section]");
		String category = categoryElement != null ? categoryElement.attr("content") : "Unknown Category";

		// 이미지 URL 추출
		Elements images = doc.select("img.image__dam-img[src]");
		String imgUrl = "%s.jpg".formatted(
			images.get(0).attr("src")
				.split(".jpg")[0]
		);
		int preWidth = getWidthOfImage(images.get(0));

		for (Element image : images) {
			if (preWidth < getWidthOfImage(image)) {
				imgUrl = "%s.jpg".formatted(
					image.attr("src")
						.split(".jpg")[0]
				);
				preWidth = getWidthOfImage(image);
			}
		}

		// 본문 추출
		Elements paragraphs = doc.select("div.article__content p");
		StringBuilder fullText = new StringBuilder();
		for (Element paragraph : paragraphs) {
			fullText.append(paragraph.text()).append(" ");
		}

		// 본문을 문장 단위로 나누기
		List<String> sentences = splitIntoSentences(fullText.toString());
		return CrawlingResponseDto.CrawlingContentResponseDto.of(
			url,
			title,
			imgUrl,
			category,
			sentences.stream()
				.map(sentence -> (Script)CNNScript.of(
						sentence, translateService.translate(sentence, "en", "ko")
					)
				).toList()
		);
	}

	@Override
	public List<Script> getYoutubeScript(String youtubeInfo, double seconds) {
		// 운영체제 감지
		String os = System.getProperty("os.name").toLowerCase();

		// 공통 옵션 설정
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--disable-gpu");
		options.addArguments("--remote-allow-origins=*");
		options.addArguments("--disable-popup-blocking");
		options.addArguments("--lang=en-US");
		options.addArguments("--start-maximized");
		options.addArguments("--headless");

		WebDriverManager.chromedriver().setup();

		if (os.contains("linux")) {
			log.info("SELENIUM : LINUX");
			// Ubuntu의 경우
			options.addArguments("--no-sandbox");
			options.addArguments("--disable-dev-shm-usage");
			options.addArguments("--ignore-ssl-errors=yes");
			options.addArguments("--ignore-certificate-errors");
			// Xvfb를 사용하는 경우
			try {
				log.info("SELENIUM : XVFB CHECK");
				if (System.getenv("DISPLAY") == null) {
					log.info("SELENIUM : XVFB NEED TO START");
					System.setProperty("DISPLAY", ":99");
					// Xvfb를 실행
					Process xvfbProcess = Runtime.getRuntime().exec("Xvfb :99 -ac &");
					xvfbProcess.waitFor();
					log.info("SELENIUM : XVFB START");
				}
				log.info("SELENIUM : XVFB RUN");
			} catch (IOException | InterruptedException e) {
				throw new CommonException(SELENIUM_RUNTIME_ERROR);
			}
		}

		WebDriver driver = new ChromeDriver(options);
		List<Script> transcriptLines;

		try {
			log.info("SELENIUM : SELENIUM DRIVER SET SUCCESS");
			transcriptLines = runSelenium(driver, youtubeInfo, seconds);
			log.info("SELENIUM : SELENIUM END");
		} catch (IOException | IllegalStateException e) {
			throw new CommonException(CRAWLING_TRANSLATE_FAILURE);
		} catch (Exception e) {
			throw new CommonException(SELENIUM_RUNTIME_ERROR);
		} finally {
			driver.quit();
			log.info("SELENIUM : DRIVER QUIT");
		}

		return transcriptLines;
	}

	@Override
	public List<Script> runSelenium(WebDriver driver, String youtubeInfo, double seconds) throws Exception {
		List<Script> scripts = new ArrayList<>();

		driver.get(youtubeInfo);
		setUpSelenium(driver);
		log.info("SELENIUM : SELENIUM DRIVER SET UP COMPLETE");

		// Use XPath to find all elements containing the transcript text
		List<WebElement> segmentElements = driver.findElements(
			By.xpath("//ytd-transcript-segment-renderer"));
		log.info("SELENIUM : START CRAWLING");
		for (int i = 0; i < segmentElements.size(); ++i) {

			String time = segmentElements.get(i)
				.findElement(By.xpath(".//div[contains(@class, 'timestamp')]"))
				.getText();
			double startTime = getSecondsFromString(time);
			double endtime = seconds;

			if (i + 1 < segmentElements.size()) {
				endtime = getSecondsFromString(
					segmentElements.get(i + 1)
						.findElement(By.xpath(".//div[contains(@class, 'timestamp')]"))
						.getText()
				);
			}

			String text = segmentElements.get(i)
				.findElement(By.xpath(".//yt-formatted-string[contains(@class, 'segment-text')]"))
				.getText();
			if (text != null && !text.isEmpty()) {
				scripts.add(
					/**
					 * 이 부분에서 null 로 한국어 부분을 저장하고 이후에 영어 스크립트를 다시 읽어 번역하고 영어 문자 비율에 맞추어
					 * 한국어를 분할해서 저장하는 방식을 생각했었습ㄴ디ㅏ
					 */
					YoutubeScript.of(
						startTime, endtime - startTime,
						text, translateService.translate(text, "en", "ko")
					)
				);

			}
		}
		log.info("SELENIUM : FINISH CRAWLING");
		return scripts;
	}

	// Internal Methods ------------------------------------------------------------------------------------------------

	// LISTENING - YOUTUBE

	private JsonNode getSnippetNode(String body) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readTree(body).path("items").get(0);
	}

	private String getThumbnailUrl(JsonNode thumbnailNode) {
		Iterator<Map.Entry<String, JsonNode>> thumbnails = thumbnailNode.fields();
		int prevWidth = 0;
		JsonNode thumbnailInfo = thumbnails.next().getValue();

		while (thumbnails.hasNext()) {
			Map.Entry<String, JsonNode> thumbnailMap = thumbnails.next();
			JsonNode comparator = thumbnailMap.getValue();
			if (Integer.parseInt(comparator.get("width").asText()) > prevWidth) {
				thumbnailInfo = comparator;
				prevWidth = Integer.parseInt(thumbnailMap.getValue().get("width").asText());
			}
		}

		return thumbnailInfo.get("url").asText();
	}

	private String extractVideoId(String youtubeUrl) {
		// Logic to extract video ID from URL
		String[] parts = youtubeUrl.split("v=");
		return parts.length > 1 ? parts[1] : "";
	}

	private String getCategoryName(String categoryId) throws Exception {
		String requestUrl =
			"https://www.googleapis.com/youtube/v3/videoCategories?part=snippet&regionCode=US&key=" + YOUTUBE_API_KEY;

		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);

		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(response.getBody());
		JsonNode itemsNode = rootNode.path("items");

		for (JsonNode item : itemsNode) {
			if (item.path("id").asText().equals(categoryId)) {
				return item.path("snippet").path("title").asText();
			}
		}

		return "Unknown Category";
	}

	private void setUpSelenium(WebDriver driver) throws InterruptedException {
		log.info("SELENIUM : SETUP SELENIUM START");

		// Initial setting
		driver.manage().window().maximize();
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
		JavascriptExecutor js = (JavascriptExecutor)driver;
		wait.until(webDriver -> js.executeScript("return document.readyState").equals("complete"));
		Thread.sleep(5000);
		log.info("SELENIUM : SETUP SUCCESS");

		// Zoom out
		js.executeScript("document.body.style.zoom='30%'");
		Thread.sleep(5000);
		log.info("SELENIUM : ZOOMOUT SUCESS");

		// Click the "expand" button to expand
		List<WebElement> expandButton
			= driver.findElements(By.xpath("//tp-yt-paper-button[@id='expand']"));
		log.info("SELENIUM : FIND EXPAND BUTTON : {} ", expandButton);
		for (WebElement button : expandButton) {
			log.info("SELENIUM : FIND BUTTON : {} ", button.getText());
			if (button.getText().contains("more")) {
				log.info("SELENIUM : FIND SUCCESS MORE BUTTON : {}", button.getText());
				js.executeScript("arguments[0].click();", button);
				log.info("SELENIUM : EXPAND BUTTON CLICK");
				break;
			}
		}

		Thread.sleep(5000);
		// Locate and click the "Show transcript" button
		log.info("SELENIUM : WAITING TRANSCRIPTION BUTTON FIND");

		WebElement transcriptButton = driver.findElement(
			By.xpath("//yt-button-shape//button[@aria-label='Show transcript']")
		);

		log.info("SELENIUM : FIND TRANSCRIPT BUTTON");
		js.executeScript("arguments[0].click();", transcriptButton);
		log.info("SELENIUM : CLICK TRANSCRIPTION");
		Thread.sleep(5000);
	}

	private double getSecondsFromString(String time) {
		return Double.parseDouble(time.split(":")[0]) * 60 + Double.parseDouble(time.split(":")[1]);
	}

	// READING - CNN
	private int getWidthOfImage(Element imginfo) {
		return Integer.parseInt(
			imginfo.attr("src")
				.split("w_")[1]
				.split(",c")[0]
		);
	}

	private List<String> splitIntoSentences(String text) {
		List<String> sentences = new ArrayList<>();

		// 정규 표현식을 사용하여 문장 단위로 나누기
		String[] splitSentences = text.split("(?<=[.!?])\\s+");

		for (String sentence : splitSentences) {
			sentences.add(sentence.trim());
		}

		return sentences;
	}

}