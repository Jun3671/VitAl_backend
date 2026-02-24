package VitAI.injevital.dto;

import lombok.Data;
import java.util.List;

@Data
public class MealPlanDto {
    private String mealType;          // 아침/점심/저녁
    private List<String> mainMenu;    // 주요 메뉴 리스트
    private String description;       // 식단 설명
    private NutritionInfoDto nutrition;  // 영양 정보
}