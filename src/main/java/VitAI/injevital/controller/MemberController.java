package VitAI.injevital.controller;

import VitAI.injevital.dto.ApiResponse;
import VitAI.injevital.dto.LoginRequest;
import VitAI.injevital.dto.MemberDTO;
import VitAI.injevital.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.LoginException;

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
    public void save(@RequestBody MemberDTO memberDTO){
        memberService.save(memberDTO);
    }
    @PostMapping("/member/login")
    public ApiResponse login(@RequestBody LoginRequest memberDTO) throws LoginException {

        return ApiResponse.success(memberService.login(memberDTO));

    }

}
