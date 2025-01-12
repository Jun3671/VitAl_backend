package VitAI.injevital.controller;

import VitAI.injevital.dto.ChatGPTRequest;
import VitAI.injevital.dto.ChatGPTResponse;
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
    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.url}")
    private String apiURL;

    @Autowired
    private RestTemplate template;

    @GetMapping("/chat")
    public String chat(@RequestParam(name = "prompt") String prompt) {
        try {
            ChatGPTRequest request = new ChatGPTRequest(model, prompt);
            log.debug("Request to ChatGPT: {}", request);

            ChatGPTResponse chatGPTResponse = template.postForObject(apiURL, request, ChatGPTResponse.class);
            log.debug("Response from ChatGPT: {}", chatGPTResponse);

            return chatGPTResponse.getChoices().get(0).getMessage().getContent();
        } catch (RestClientException e) {
            log.error("ChatGPT API 호출 실패: ", e);
            return "챗봇 서비스 오류 발생";
        } catch (Exception e) {
            log.error("예상치 못한 에러: ", e);
            return "서버 오류 발생";
        }
    }
}