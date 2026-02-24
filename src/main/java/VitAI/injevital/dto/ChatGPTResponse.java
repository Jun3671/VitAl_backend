package VitAI.injevital.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)  // 알 수 없는 필드 무시
public class ChatGPTResponse {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choice> choices;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)  // 중첩 객체에도 적용
    public static class Choice {
        private int index;
        private Message message;
        private String finish_reason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)  // 중첩 객체에도 적용
    public static class Message {
        private String role;
        private String content;
    }
}