package VitAI.injevital.controller;

import VitAI.injevital.dto.*;
import VitAI.injevital.entity.Member;
import VitAI.injevital.jwt.JwtFilter;
import VitAI.injevital.service.EmailService;
import VitAI.injevital.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.LoginException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {
    private final MemberService memberService;
    private final EmailService emailService;

    @GetMapping("/member/save")
    public String saveForm(){
        return "save";
    }
    @PostMapping("/member/save")
    public void save(@RequestBody MemberDTO memberDTO){
            memberService.save(memberDTO);
    }

    @GetMapping("/member/login")
    public String loginForm(){
        return "login";
    }


    @PostMapping("/member/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse loginResponse = memberService.login(loginRequest);

            // 헤더 설정
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + loginResponse.getTokenDto().getToken());

            // ApiResponse 성공 응답 생성
            ApiResponse response = ApiResponse.success(loginResponse);

            return new ResponseEntity<>(response, httpHeaders, HttpStatus.OK);

        } catch (Exception e) {
            // ApiResponse 에러 응답 생성
            ApiResponse response = ApiResponse.error(e.getMessage());

            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }


    @GetMapping("/check-id")
    public ApiResponse checkMemberEmail(@RequestParam String id) {
        try {
            boolean exists = emailService.isIdExist(id);
            if (exists) {
                return ApiResponse.error("이미 사용 중인 아이디입니다.");
            } else {
                return ApiResponse.success("사용 가능한 아이디입니다.");
            }
        } catch (Exception e) {
            return ApiResponse.error("아이디 중복 확인 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/update/physical-info")
    public ApiResponse updatePhysicalInfo(@RequestBody MemberDTO memberDTO) {
        try {
            memberService.updatePhysicalInfo(memberDTO);
            return ApiResponse.success("회원 정보가 수정되었습니다");
        } catch (Exception e) {
            return ApiResponse.error("회원 정보 수정 실패: " + e.getMessage());
        }
    }

    @GetMapping("/member/body-info")
    public ResponseEntity<ApiResponse> getMemberBodyInfo(
            @AuthenticationPrincipal UserDetails userDetails) {
        MemberBodyInfoDTO bodyInfo = memberService.getBodyInfo(userDetails.getUsername());

        if (bodyInfo != null) {
            return ResponseEntity.ok(ApiResponse.success(bodyInfo));
        } else {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("회원 정보를 찾을 수 없습니다."));
        }
    }


}
