package VitAI.injevital.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Configuration
public class GeminiConfig {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Bean
    public RestTemplate template() {
        RestTemplate restTemplate = new RestTemplate();

        ClientHttpRequestInterceptor interceptor = (request, body, execution) -> {
            // API 키 로깅 (실제 값은 보안을 위해 마스킹)
            log.debug("API Key present: {}", geminiApiKey != null && !geminiApiKey.isEmpty());

            // Gemini API는 x-goog-api-key 헤더 사용 (Bearer 토큰 방식이 아님)
            request.getHeaders().set("x-goog-api-key", geminiApiKey);
            request.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            // 요청 로깅
            log.debug("Making request to: {}", request.getURI());
            log.debug("Request headers: {}", request.getHeaders());

            // 요청 실행
            return execution.execute(request, body);
        };

        restTemplate.getInterceptors().add(interceptor);
        return restTemplate;
    }

    @PostConstruct
    void init() {
        log.debug("Gemini API Key length: {}", geminiApiKey != null ? geminiApiKey.length() : 0);
    }
}
