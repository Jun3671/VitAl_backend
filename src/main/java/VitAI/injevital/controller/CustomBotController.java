package VitAI.injevital.controller;

import VitAI.injevital.dto.GeminiRequest;
import VitAI.injevital.dto.GeminiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RestController
@Slf4j
@RequestMapping("/bot")
public class CustomBotController {
    @Value("${gemini.model}")
    private String model;

    @Value("${gemini.api.url}")
    private String apiURL;

    @Autowired
    private RestTemplate template;

    @GetMapping("/chat")
    public String chat(@RequestParam(name = "prompt") String prompt) {
        try {
            GeminiRequest request = new GeminiRequest(prompt);
            log.debug("Request to Gemini: {}", request);

            GeminiResponse geminiResponse = template.postForObject(apiURL, request, GeminiResponse.class);
            log.debug("Response from Gemini: {}", geminiResponse);

            return geminiResponse.getTextContent();
        } catch (RestClientException e) {
            log.error("Gemini API 호출 실패: ", e);
            return "챗봇 서비스 오류 발생";
        } catch (Exception e) {
            log.error("예상치 못한 에러: ", e);
            return "서버 오류 발생";
        }
    }
}