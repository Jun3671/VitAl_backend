package VitAI.injevital.controller;

import VitAI.injevital.dto.ApiResponse;
import VitAI.injevital.dto.LoginRequest;
import VitAI.injevital.dto.MemberDTO;
import VitAI.injevital.entity.Member;
import VitAI.injevital.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/member/save")
    public String saveForm(){
        return "save";
    }
    @PostMapping("/member/save")
    public ApiResponse save(@RequestBody MemberDTO memberDTO){
        try {
            memberService.save(memberDTO);
            return ApiResponse.success("회원가입 성공");
        } catch (Exception e) {
            return ApiResponse.error("회원가입 실패: " + e.getMessage());
        }
    }

    @GetMapping("/member/login")
    public String loginForm(){
        return "login";
    }
    @PostMapping("/member/login")
    public ApiResponse login(@RequestBody LoginRequest memberDTO) {
        try {
            System.out.println(memberDTO.getMemberId());
            memberService.login(memberDTO);
            return ApiResponse.success("로그인 성공");
        } catch (Exception e) {
            return ApiResponse.error("로그인 실패: " + e.getMessage());
        }
    }


    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<Member> getMyUserInfo() {
        return ResponseEntity.ok(memberService.getMyUserWithAuthorities().get());
    }

    @GetMapping("/user/{username}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Member> getUserInfo(@PathVariable String username) {
        return ResponseEntity.ok(memberService.getUserWithAuthorities(username).get());
    }
}
