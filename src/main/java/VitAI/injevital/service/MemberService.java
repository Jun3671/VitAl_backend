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
        log.info("로그인 서비스 시작");

        // memberId로 회원 찾기
        Optional<Member> byMemberId = memberRepository.findByMemberId(memberDTO.getMemberId());

        // 회원이 존재하지 않는 경우
        if (byMemberId.isEmpty()) {
            throw new LoginException("존재하지 않는 회원 아이디입니다.");
        }

        Member member = byMemberId.get();
        log.info("회원 조회 성공: {}", member.getMemberId());

        // PasswordEncoder를 사용하여 비밀번호 검증
        if (!passwordEncoder.matches(memberDTO.getMemberPassword(), member.getMemberPassword())) {
            throw new LoginException("비밀번호가 일치하지 않습니다.");
        }
        log.info("비밀번호 검증 성공");

        // 권한 정보 생성
        List<GrantedAuthority> grantedAuthorities = member.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthorityName()))
                .collect(Collectors.toList());
        log.info("권한 정보 생성: {}", grantedAuthorities);

        // Authentication 객체 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                member.getMemberId(),
                null,
                grantedAuthorities
        );

        // JWT 토큰 생성
        String token = tokenProvider.createToken(authentication);
        log.info("토큰 생성 성공");

        // 응답 데이터 생성
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setMemberInfo(MemberDTO.toMemberDTO(member));
        loginResponse.setTokenDto(new TokenDto(token));

        return loginResponse;
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

        // 신체 정보 유효성 검사
        validatePhysicalInfo(memberDTO);

        // 기본 신체 정보 업데이트
        member.setMemberHeight(memberDTO.getMemberHeight());
        member.setMemberWeight(memberDTO.getMemberWeight());

        // BMI 계산 및 업데이트
        updateBMI(member);

        // 추가 신체 정보 업데이트
        updateAdditionalPhysicalInfo(member, memberDTO);

        // 변경된 정보 저장
        Member updatedMember = memberRepository.save(member);
        return MemberDTO.toMemberDTO(updatedMember);
    }

    // 신체 정보 유효성 검사
    private void validatePhysicalInfo(MemberDTO memberDTO) throws Exception {
        if (memberDTO.getMemberHeight() <= 0 || memberDTO.getMemberWeight() <= 0) {
            throw new Exception("키와 체중은 0보다 커야 합니다.");
        }
    }

    // BMI 계산 및 업데이트
    private void updateBMI(Member member) {
        double heightInMeter = member.getMemberHeight() / 100;
        double bmi = member.getMemberWeight() / (heightInMeter * heightInMeter);
        member.setMemberBmi(Math.round(bmi * 100) / 100.0);  // 소수점 둘째자리까지
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
    }
    public MemberBodyInfoDTO getBodyInfo(String Id) {
        Optional<Member> member = memberRepository.findByMemberId(Id);
        return modelMapper.map(member, MemberBodyInfoDTO.class);
    }

}
