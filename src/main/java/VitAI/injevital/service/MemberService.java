package VitAI.injevital.service;

import VitAI.injevital.dto.LoginRequest;
import VitAI.injevital.dto.MemberDTO;
import VitAI.injevital.entity.Authority;
import VitAI.injevital.entity.Member;
import VitAI.injevital.jwt.SecurityUtil;
import VitAI.injevital.repository.AuthorityRepository;
import VitAI.injevital.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor

public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthorityRepository authorityRepository;
    public void save(MemberDTO memberDTO){

        Authority authority = Authority.builder()
                .authorityName("ROLE_USER")
                .build();
        authorityRepository.save(authority);

        //repository save 메서드 호출
        Member memberEntity = Member.toMemberEntity(memberDTO , authority , passwordEncoder);
        memberRepository.save(memberEntity);
    }

    public MemberDTO login(LoginRequest memberDTO){
        Optional<Member> byMemberEmail = memberRepository.findByMemberEmail(memberDTO.getMemberEmail());
        System.out.println(byMemberEmail);
        if(byMemberEmail.isPresent()){
            Member member = byMemberEmail.get();
            // 비밀번호 비교
            if (passwordEncoder.matches(memberDTO.getMemberPassword(), member.getMemberPassword())) {
                return MemberDTO.toMemberDTO(member);
            } else {
                // 비밀번호 불일치
                return null;
            }
        } else {
            // 이메일이 존재하지 않는 경우 처리
            return null;
        }

    }

    @Transactional(readOnly = true)
    public Optional<Member> getUserWithAuthorities(String username) {
        return memberRepository.findOneWithAuthoritiesByMemberName(username);
    }

    // 현재 securityContext에 저장된 username의 정보만 가져오는 메소드
    @Transactional(readOnly = true)
    public Optional<Member> getMyUserWithAuthorities() {
        return SecurityUtil.getCurrentUsername()
                .flatMap(memberRepository::findOneWithAuthoritiesByMemberName);
    }
}
