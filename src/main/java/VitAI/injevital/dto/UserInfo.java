package VitAI.injevital.dto;

import lombok.Data;

@Data
public class UserInfo {
    private double height;
    private double weight;
    private double bmi;
    private double dailyCalorieNeeds;
    private String goal;
    private String preferredFoodType;
}