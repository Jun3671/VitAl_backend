package VitAI.injevital.service;

import VitAI.injevital.dto.LoginRequest;
import VitAI.injevital.dto.MemberDTO;
import VitAI.injevital.entity.Member;
import VitAI.injevital.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.security.auth.login.LoginException;
import java.util.Optional;

@Service
@RequiredArgsConstructor

public class MemberService {

    private final MemberRepository memberRepository;

    public void save(MemberDTO memberDTO) {
        //repository save 메서드 호출
        Member memberEntity = Member.toMemberEntity(memberDTO);
        memberRepository.save(memberEntity);
    }

    public MemberDTO login(LoginRequest memberDTO) throws LoginException {
        // memberId로 회원 찾기
        Optional<Member> byMemberId = memberRepository.findByMemberId(memberDTO.getMemberId());

        // 회원이 존재하지 않는 경우
        if (byMemberId.isEmpty()) {
            throw new LoginException("존재하지 않는 회원 아이디입니다.");
        }

        Member member = byMemberId.get();

        // 비밀번호가 일치하지 않는 경우
        if (!member.getMemberPassword().equals(memberDTO.getMemberPassword())) {
            throw new LoginException("비밀번호가 일치하지 않습니다.");
        }

        // 로그인 성공
        return MemberDTO.toMemberDTO(member);
    }

    // 컨트롤러에서 예외 처리
    @ExceptionHandler(LoginException.class)
    public ResponseEntity<String> handleLoginException(LoginException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(e.getMessage());

    }
}