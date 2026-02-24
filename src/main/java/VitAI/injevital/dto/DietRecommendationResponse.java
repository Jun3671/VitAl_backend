// 응답 DTO
package VitAI.injevital.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DietRecommendationResponse {
    private UserInfo userInfo;
    private List<MealPlanDto> meals;
    private List<String> healthAdvice;
    private NutritionInfoDto dailyNutritionSummary;
}