package VitAI.injevital.controller;

import VitAI.injevital.dto.*;
import VitAI.injevital.entity.Member;
import VitAI.injevital.jwt.TokenProvider;
import VitAI.injevital.repository.MemberRepository;
import VitAI.injevital.service.DietService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bot")
public class DietAIController {

    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;
    private final DietService dietService;

    @PostMapping("/diet-recommendation")
    public ResponseEntity<DietRecommendationResponse> getDietRecommendation(
            @RequestBody DietRecommendationRequest request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7); // "Bearer " 이후의 토큰 추출

        // 토큰 유효성 검증
        if (!tokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 토큰에서 memberId 추출
        String memberId = tokenProvider.getMemberIdFromToken(token);

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        try {
            DietRecommendationResponse response = dietService.generateDietRecommendation(member, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}