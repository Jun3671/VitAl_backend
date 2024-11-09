package VitAI.injevital.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import VitAI.injevital.entity.Member;
import org.checkerframework.checker.units.qual.N;
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
    private double memberHeight;

    @NotBlank
    @Size(min = 3 , max = 50)
    private double memberWeight;


    @NotBlank
    @Size(min = 3 , max = 50)
    private double memberSmm;   // 골격근량


    @NotBlank
    @Size(min = 3 , max = 50)
    private double memberBfm;   //체지방량

    @NotBlank
    @Size(min = 3 , max = 50)
    private double memberBfp;   // 체지방률

    @NotBlank
    @Size(min = 3 , max = 50)
    private double memberBmi;   // bmi




    public static MemberDTO toMemberDTO(Member memberEntity) {

        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setMemberGender(memberEntity.getMemberGender());
        memberDTO.setMemberEmail(memberEntity.getMemberEmail());
        memberDTO.setMemberPassword(memberEntity.getMemberPassword());
        memberDTO.setMemberName(memberEntity.getMemberName());
        memberDTO.setMemberId(memberEntity.getMemberId());
        memberDTO.setMemberHeight(memberEntity.getMemberHeight());
        memberDTO.setMemberWeight(memberEntity.getMemberWeight());
        return memberDTO;
    }


}
