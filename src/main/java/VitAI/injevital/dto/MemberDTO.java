package VitAI.injevital.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
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


    private double memberHeight;


    private double memberWeight;


    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double memberSmm;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double memberBfm;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double memberBfp;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double memberBmi;




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
