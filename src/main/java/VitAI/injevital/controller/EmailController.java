package VitAI.injevital.controller;

import VitAI.injevital.dto.EmailDto;

import VitAI.injevital.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import VitAI.injevital.dto.EmailVerificationDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.UnsupportedEncodingException;
import org.springframework.beans.factory.annotation.Autowired;


@NoArgsConstructor
@AllArgsConstructor

@RestController
public class EmailController {

    @Autowired
    private EmailService emailService;  // EmailService 의존성 주입

    @ResponseBody
    @PostMapping("/sign-up/emailCheck") // 이 부분은 각자 바꿔주시면 됩니다.
    public String emailCheck(@RequestBody EmailDto emailCheckReq) throws MessagingException, UnsupportedEncodingException {
        // 이메일 인증 코드 생성
        String authCode = emailService.sendEmail(emailCheckReq.getEmail());
        // 인증 코드를 직접 반환
        return authCode;
    }
    @PostMapping("/sign-up/verifyEmail")
    public ResponseEntity<String> verifyEmail(@RequestBody EmailVerificationDto verificationDto) {
        boolean isVerified = emailService.verifyAuthCode(verificationDto.getEmail(), verificationDto.getAuthCode());
        if (isVerified) {
            return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
        } else {
            return ResponseEntity.badRequest().body("인증 코드가 올바르지 않습니다.");
        }
    }
}

