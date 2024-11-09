package VitAI.injevital.controller;

import VitAI.injevital.dto.EmailDto;

import VitAI.injevital.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import VitAI.injevital.dto.EmailVerificationDto;
import org.springframework.http.HttpStatus;
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
    @PostMapping("/sign-up/emailCheck")
    public ResponseEntity<String> emailCheck(@RequestBody EmailDto emailCheckReq) throws MessagingException, UnsupportedEncodingException {
        try {
            // 이메일 인증 코드 생성
            String authCode = emailService.sendEmail(emailCheckReq.getEmail());

            // 인증 코드 반환, 상태 코드 200 OK와 함께
            return ResponseEntity.ok(authCode);
        } catch (Exception e) {
            // 예외 처리: 오류 발생 시 500 Internal Server Error 상태 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("인증 코드 전송 실패");
        }
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

