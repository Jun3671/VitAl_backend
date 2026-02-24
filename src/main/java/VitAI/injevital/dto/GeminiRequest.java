package VitAI.injevital.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class GeminiRequest {
    private List<GeminiContent> contents;
    private GenerationConfig generationConfig;

    public GeminiRequest(String prompt) {
        this.contents = new ArrayList<>();

        // Create parts list with text
        List<GeminiPart> parts = new ArrayList<>();
        parts.add(new GeminiPart(prompt));

        // Add user content
        this.contents.add(new GeminiContent("user", parts));

        // Set default generation config
        this.generationConfig = new GenerationConfig();
    }

    public GeminiRequest(String prompt, double temperature, int maxTokens) {
        this(prompt);
        this.generationConfig = new GenerationConfig(temperature, maxTokens);
    }

    @Data
    @NoArgsConstructor
    public static class GenerationConfig {
        private Double temperature = 0.7;
        private Integer maxOutputTokens = 2000;
        private String responseMimeType = "application/json";

        public GenerationConfig(double temperature, int maxOutputTokens) {
            this.temperature = temperature;
            this.maxOutputTokens = maxOutputTokens;
            this.responseMimeType = "application/json";
        }
    }
}
