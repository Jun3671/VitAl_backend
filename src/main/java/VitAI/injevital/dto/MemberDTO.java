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
    private String memberEmail;

    @NotBlank
    @Size(min = 3 , max = 100)
    private String memberPassword;

    @NotBlank
    @Size(min = 3 , max = 50)
    private String memberName;

    @NotBlank
    @Size(min = 3 , max = 50)
    private String memberNumber;

    private String memberHeight;

    private String memberWeight;


    public static MemberDTO toMemberDTO(Member memberEntity) {

        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setMemberEmail(memberEntity.getMemberEmail());
        memberDTO.setMemberPassword(passwordEncoder.encode(memberDTO.getMemberPassword()));
        memberDTO.setMemberName(memberEntity.getMemberName());
        memberDTO.setMemberNumber(memberEntity.getMemberNumber());
        memberDTO.setMemberHeight(memberEntity.getMemberHeight());
        memberDTO.setMemberWeight(memberEntity.getMemberWeight());
        return memberDTO;
    }


}
