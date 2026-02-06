package VitAI.injevital.service;
import VitAI.injevital.entity.EmailAuthCode;
import VitAI.injevital.repository.EmailAuthCodeRepository;
import VitAI.injevital.repository.MemberRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class EmailService {

    private final MemberRepository memberRepository;
    private final JavaMailSender emailSender;
    private final EmailAuthCodeRepository emailAuthCodeRepository;

    private static final int AUTH_CODE_EXPIRATION_MINUTES = 5;
    private String authNum; // 인증 번호

    // 인증번호 8자리 무작위 생성
    public void createCode() {
        Random random = new Random();
        StringBuffer key = new StringBuffer();

        for(int i=0; i<8; i++) {
            int idx = random.nextInt(3);

            switch (idx) {
                case 0 :
                    key.append((char) ((int)random.nextInt(26) + 97));
                    break;
                case 1:
                    key.append((char) ((int)random.nextInt(26) + 65));
                    break;
                case 2:
                    key.append(random.nextInt(9));
                    break;
            }
        }
        authNum = key.toString();
    }

    // 메일 양식 작성
    public MimeMessage createEmailForm(String email) throws MessagingException, UnsupportedEncodingException {
        createCode();
        String setFrom = "testtest12@gmail.com";
        String toEmail = email;
        String title = "데옹 인증번호 테스트";

        MimeMessage message = emailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, toEmail);
        message.setSubject(title);

        // 메일 내용
        String msgOfEmail="";
        msgOfEmail += "<div style='margin:20px;'>";
        msgOfEmail += "<h1> 안녕하세요 VitAi 입니다. </h1>";
        msgOfEmail += "<br>";
        msgOfEmail += "<p>아래 코드를 입력해주세요<p>";
        msgOfEmail += "<br>";
        msgOfEmail += "<p>감사합니다.<p>";
        msgOfEmail += "<br>";
        msgOfEmail += "<div align='center' style='border:1px solid black; font-family:verdana';>";
        msgOfEmail += "<h3 style='color:blue;'>회원가입 인증 코드입니다.</h3>";
        msgOfEmail += "<div style='font-size:130%'>";
        msgOfEmail += "CODE : <strong>";
        msgOfEmail += authNum + "</strong><div><br/> ";
        msgOfEmail += "</div>";

        message.setFrom(setFrom);
        message.setText(msgOfEmail, "utf-8", "html");

        return message;
    }

    public String sendEmail(String email) throws MessagingException, UnsupportedEncodingException {
        MimeMessage emailForm = createEmailForm(email);
        emailSender.send(emailForm);

        // 기존 인증코드가 있으면 삭제
        emailAuthCodeRepository.findByEmail(email)
                .ifPresent(emailAuthCodeRepository::delete);

        // 새로운 인증코드 저장
        EmailAuthCode emailAuthCode = EmailAuthCode.builder()
                .email(email)
                .authCode(authNum)
                .expiresAt(LocalDateTime.now().plusMinutes(AUTH_CODE_EXPIRATION_MINUTES))
                .build();
        emailAuthCodeRepository.save(emailAuthCode);

        return authNum;
    }

    public boolean verifyAuthCode(String email, String code) {
        return emailAuthCodeRepository.findByEmail(email)
                .map(emailAuthCode -> {
                    if (emailAuthCode.isCodeValid(code)) {
                        emailAuthCodeRepository.delete(emailAuthCode);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    public boolean isIdExist(String id) {
        return memberRepository.existsByMemberId(id);
    }

    // 매 시간마다 만료된 인증코드 삭제
    @Scheduled(cron = "0 0 * * * *")
    public void deleteExpiredAuthCodes() {
        emailAuthCodeRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }

}