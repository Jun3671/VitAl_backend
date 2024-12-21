package VitAI.injevital.service;

import VitAI.injevital.dto.*;
import VitAI.injevital.entity.Authority;
import VitAI.injevital.entity.Member;
import VitAI.injevital.jwt.SecurityUtil;
import VitAI.injevital.jwt.TokenProvider;
import VitAI.injevital.repository.AuthorityRepository;
import VitAI.injevital.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.helpers.AbstractLogger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.LoginException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthorityRepository authorityRepository;
    private final ModelMapper modelMapper;
    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    
    private AbstractLogger log;


    public void save(MemberDTO memberDTO){
        Authority authority = Authority.builder()
                .authorityName("ROLE_USER")
                .build();
        authorityRepository.save(authority);

        //repository save 메서드 호출
        Member memberEntity = Member.toMemberEntity(memberDTO , passwordEncoder , authority );
        memberRepository.save(memberEntity);
    }

    public LoginResponse login(LoginRequest memberDTO) throws LoginException {
        try {
            // Authentication 객체 생성
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(memberDTO.getMemberId(), memberDTO.getMemberPassword());

            // 인증 처리
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 토큰 생성
            String jwt = tokenProvider.createToken(authentication);

            // 회원 정보 조회
            Member member = memberRepository.findByMemberId(memberDTO.getMemberId())
                    .orElseThrow(() -> new LoginException("존재하지 않는 회원입니다."));

            // 응답 데이터 생성
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setMemberInfo(MemberDTO.toMemberDTO(member));
            loginResponse.setTokenDto(new TokenDto(jwt));

            return loginResponse;
        } catch (Exception e) {
            throw new LoginException(e.getMessage());
        }
    }

    @Transactional
    public MemberDTO updatePhysicalInfo(MemberDTO memberDTO) throws Exception {
        // 현재 로그인한 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new Exception("로그인이 필요합니다.");
        }

        // UserDetails에서 현재 로그인한 사용자의 username 가져오기
        String currentUsername = authentication.getName();

        // 현재 로그인한 사용자의 정보 조회
        Member member = memberRepository.findByMemberId(currentUsername)
                .orElseThrow(() -> new Exception("회원 정보를 찾을 수 없습니다."));

        // 기본 신체 정보 업데이트
        member.setMemberHeight(memberDTO.getMemberHeight());
        member.setMemberWeight(memberDTO.getMemberWeight());

        // BMI 계산 및 업데이트 (BMI = 체중(kg) / (신장(m) * 신장(m)))
        double heightInMeters = memberDTO.getMemberHeight() / 100.0; // cm를 m로 변환
        double bmi = memberDTO.getMemberWeight() / (heightInMeters * heightInMeters);
        // 소수점 첫째자리까지 반올림
        bmi = Math.round(bmi * 10) / 10.0;
        member.setMemberBmi(bmi);

        // 추가 신체 정보 업데이트
        updateAdditionalPhysicalInfo(member, memberDTO);

        // 변경된 정보 저장
        Member updatedMember = memberRepository.save(member);
        return MemberDTO.toMemberDTO(updatedMember);
    }

    // 추가 신체 정보 업데이트
    private void updateAdditionalPhysicalInfo(Member member, MemberDTO memberDTO) throws Exception {
        // 골격근량 업데이트
        if (memberDTO.getMemberSmm() != null) {
            if (memberDTO.getMemberSmm() < 0) {
                throw new Exception("골격근량은 0보다 작을 수 없습니다.");
            }
            member.setMemberSmm(memberDTO.getMemberSmm());
        }

        // 체지방량 업데이트
        if (memberDTO.getMemberBfm() != null) {
            if (memberDTO.getMemberBfm() < 0) {
                throw new Exception("체지방량은 0보다 작을 수 없습니다.");
            }
            member.setMemberBfm(memberDTO.getMemberBfm());
        }

        // 체지방률 업데이트
        if (memberDTO.getMemberBfp() != null) {
            if (memberDTO.getMemberBfp() < 0 || memberDTO.getMemberBfp() > 100) {
                throw new Exception("체지방률은 0에서 100 사이여야 합니다.");
            }
            member.setMemberBfp(memberDTO.getMemberBfp());
        }
        // Bmi 업데이트
        if (memberDTO.getMemberBmi() != null) {
            if (memberDTO.getMemberBmi() < 0) {
                throw new Exception("골격근량은 0보다 작을 수 없습니다.");
            }
            member.setMemberBmi(memberDTO.getMemberBmi());
        }

    }
    public MemberBodyInfoDTO getBodyInfo(String memberId) {
        return memberRepository.findByMemberId(memberId)
                .map(member -> modelMapper.map(member, MemberBodyInfoDTO.class))
                .orElse(null);
    }

}
