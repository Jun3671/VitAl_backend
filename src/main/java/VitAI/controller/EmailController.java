package VitAI.controller;

import VitAI.dto.EmailDto;
import VitAI.dto.EmailVerificationDto;
import VitAI.service.EmailService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

@RestController
public class EmailController {

    @Autowired
    private EmailService emailService;

    @ResponseBody
    @PostMapping("/sign-up/emailCheck")
    public String emailCheck(@RequestBody EmailDto emailCheckReq) throws MessagingException, UnsupportedEncodingException {
        return emailService.sendEmail(emailCheckReq.getEmail());
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