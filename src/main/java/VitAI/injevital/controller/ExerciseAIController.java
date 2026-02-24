package VitAI.injevital.controller;

import VitAI.injevital.dto.*;
import VitAI.injevital.service.ExerciseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exercise")
public class ExerciseAIController {

    @Autowired
    private ExerciseService exerciseService;

    @PostMapping("/recommend")
    public ResponseEntity<ExerciseRecommendationResponse> recommendExercises(
            @RequestBody ExerciseRecommendationRequest request) {
        ExerciseRecommendationResponse response = exerciseService.getExerciseRecommendations(request);
        return ResponseEntity.ok(response);
    }
}