package VitAI.injevital.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import VitAI.injevital.entity.Member;
import org.springframework.security.crypto.password.PasswordEncoder;

@NoArgsConstructor
@Getter
@Setter
@ToString


public class MemberDTO {

    private static PasswordEncoder passwordEncoder;


    @NotBlank
    @Size(min = 3 , max = 50)
    private String memberGender;

    @NotBlank
    @Size(min = 3 , max = 50)
    private String memberEmail;

    @NotBlank
    @Size(min = 3 , max = 100)
    private String memberPassword;

    @NotBlank
    @Size(min = 3 , max = 50)
    private String memberName;

    @NotBlank
    @Size(min = 3 , max = 50)
    private String memberId;

    @NotBlank
    @Size(min = 3 , max = 50)
    private String memberHeight;

    @NotBlank
    @Size(min = 3 , max = 50)
    private String memberWeight;


    public static MemberDTO toMemberDTO(Member memberEntity) {

        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setMemberGender(memberEntity.getMemberGender());
        memberDTO.setMemberEmail(memberEntity.getMemberEmail());
        // 비밀번호는 DTO에서 제외하거나, 로그인 시 비밀번호 비교에만 사용해야 하므로 설정하지 않음
        memberDTO.setMemberName(memberEntity.getMemberName());
        memberDTO.setMemberId(memberEntity.getMemberId());
        memberDTO.setMemberHeight(memberEntity.getMemberHeight());
        memberDTO.setMemberWeight(memberEntity.getMemberWeight());
        return memberDTO;
    }


}
