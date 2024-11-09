package VitAI.injevital.controller;

import VitAI.injevital.dto.ApiResponse;
import VitAI.injevital.dto.LoginRequest;
import VitAI.injevital.dto.MemberDTO;
import VitAI.injevital.entity.Member;
import VitAI.injevital.service.EmailService;
import VitAI.injevital.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.User;
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
    public ApiResponse login(@RequestBody LoginRequest memberDTO) throws LoginException {
        try {
            memberService.login(memberDTO);
            return ApiResponse.success("로그인 성공");
        } catch (Exception e) {
            return ApiResponse.error("로그인 실패: " + e.getMessage());
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
    public ApiResponse updatePhysicalInfo(
            @RequestBody MemberDTO memberDTO) {
        try {
            memberService.updatePhysicalInfo(memberDTO, memberDTO.getMemberId());
            return ApiResponse.success("회원 정보가 수정되었습니다");
        } catch (Exception e) {
            return ApiResponse.error("회원 정보 수정 실패: " + e.getMessage());
        }
    }
}
