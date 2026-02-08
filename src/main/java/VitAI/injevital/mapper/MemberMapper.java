package VitAI.injevital.mapper;

import VitAI.injevital.dto.MemberDTO;
import VitAI.injevital.entity.Authority;
import VitAI.injevital.entity.Member;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Member 엔티티와 DTO 간의 변환을 담당하는 매퍼 클래스
 */
@Component
public class MemberMapper {

    /**
     * MemberDTO를 Member 엔티티로 변환합니다.
     *
     * @param memberDTO 변환할 MemberDTO
     * @param passwordEncoder 비밀번호 인코더
     * @param authority 권한
     * @return 변환된 Member 엔티티
     */
    public Member toEntity(MemberDTO memberDTO, PasswordEncoder passwordEncoder, Authority authority) {
        double bmi = calculateBmi(memberDTO.getMemberHeight(), memberDTO.getMemberWeight());

        return Member.builder()
                .authorities(Collections.singleton(authority))
                .memberId(memberDTO.getMemberId())
                .memberWeight(memberDTO.getMemberWeight())
                .memberHeight(memberDTO.getMemberHeight())
                .memberBmi(bmi)
                .memberPassword(passwordEncoder.encode(memberDTO.getMemberPassword()))
                .memberGender(memberDTO.getMemberGender())
                .memberName(memberDTO.getMemberName())
                .memberEmail(memberDTO.getMemberEmail())
                .activated(true)
                .build();
    }

    /**
     * Member 엔티티를 MemberDTO로 변환합니다.
     *
     * @param member 변환할 Member 엔티티
     * @return 변환된 MemberDTO
     */
    public MemberDTO toDto(Member member) {
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setMemberGender(member.getMemberGender());
        memberDTO.setMemberEmail(member.getMemberEmail());
        memberDTO.setMemberPassword(member.getMemberPassword());
        memberDTO.setMemberName(member.getMemberName());
        memberDTO.setMemberId(member.getMemberId());
        memberDTO.setMemberHeight(member.getMemberHeight());
        memberDTO.setMemberWeight(member.getMemberWeight());
        return memberDTO;
    }

    /**
     * BMI(체질량지수)를 계산합니다.
     * BMI = 체중(kg) / (신장(m))^2
     *
     * @param height 키 (cm)
     * @param weight 체중 (kg)
     * @return 계산된 BMI (소수점 첫째자리까지)
     */
    public double calculateBmi(double height, double weight) {
        double heightInMeters = height / 100.0;
        return Math.round((weight / (heightInMeters * heightInMeters)) * 10) / 10.0;
    }
}
