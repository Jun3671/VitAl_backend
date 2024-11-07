package VitAI.injevital.service;

import VitAI.injevital.dto.LoginRequest;
import VitAI.injevital.dto.MemberDTO;
import VitAI.injevital.entity.Authority;
import VitAI.injevital.entity.Member;
import VitAI.injevital.repository.AuthorityRepository;
import VitAI.injevital.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.security.auth.login.LoginException;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor

public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    public void save(MemberDTO memberDTO){
        //repository save 메서드 호출
        Member memberEntity = Member.toMemberEntity(memberDTO , passwordEncoder);
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

        // PasswordEncoder를 사용하여 비밀번호 검증
        if (!passwordEncoder.matches(memberDTO.getMemberPassword(), member.getMemberPassword())) {
            throw new LoginException("비밀번호가 일치하지 않습니다.");
        }

        // 로그인 성공
        return MemberDTO.toMemberDTO(member);
    }





}
