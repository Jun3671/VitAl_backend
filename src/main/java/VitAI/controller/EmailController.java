package VitAI.controller;

import VitAI.dto.EmailDto;
import VitAI.service.EmailService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

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
}
