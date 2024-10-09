package VitAI.injevital.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import VitAI.injevital.entity.Member;

@NoArgsConstructor
@Getter
@Setter
@ToString


public class MemberDTO {


    @NotBlank
    @Size(min = 3 , max = 50)
    private String memberId;

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
    private String memberHeight;

    @NotBlank
    @Size(min = 3 , max = 50)
    private String memberWeight;

    @NotBlank
    @Size(min = 1, max = 50)
    private String skeletalMuscleMass;

    @NotBlank
    @Size(min = 1, max = 50)
    private String bodyFatMass;

    @NotBlank
    @Size(min = 1, max = 50)
    private String bodyFatPercentage;

    @NotBlank
    @Size(min = 1, max = 50)
    private String bmi;

    public static MemberDTO toMemberDTO(Member memberEntity) {

        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setMemberEmail(memberEntity.getMemberEmail());
        memberDTO.setMemberPassword(memberEntity.getMemberPassword());
        memberDTO.setMemberName(memberEntity.getMemberName());
        memberDTO.setMemberHeight(memberEntity.getMemberHeight());
        memberDTO.setMemberWeight(memberEntity.getMemberWeight());
        memberDTO.setMemberId(memberEntity.getMemberId());
        memberDTO.setSkeletalMuscleMass(memberEntity.getSkeletalMuscleMass());
        memberDTO.setBodyFatMass(memberEntity.getBodyFatMass());
        memberDTO.setBodyFatPercentage(memberEntity.getBodyFatPercentage());
        memberDTO.setBmi(memberEntity.getBmi());
        return memberDTO;
    }


}
