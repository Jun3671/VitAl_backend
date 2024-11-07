package VitAI.injevital.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DietRecommendationRequest {
    private String memberId;    // 아이디
    private String foodType;    // 음식 종류
    private String goal;        // 목표

}