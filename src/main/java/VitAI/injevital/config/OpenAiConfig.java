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
public class OpenAiConfig {

    @Value("${openai.api.key}")
    private String openAiKey;

    @Bean
    public RestTemplate template() {
        RestTemplate restTemplate = new RestTemplate();

        ClientHttpRequestInterceptor interceptor = (request, body, execution) -> {
            // API 키 로깅 (실제 값은 보안을 위해 마스킹)
            log.debug("API Key present: {}", openAiKey != null && !openAiKey.isEmpty());

            // 헤더 설정
            request.getHeaders().setBearerAuth(openAiKey);
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
        log.debug("API Key length: {}", openAiKey != null ? openAiKey.length() : 0);
    }
}