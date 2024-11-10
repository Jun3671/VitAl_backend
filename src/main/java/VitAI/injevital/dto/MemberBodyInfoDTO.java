package VitAI.injevital.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberBodyInfoDTO {
    private String memberName;
    private String memberEmail;
    private String memberGender;
    private double memberHeight;
    private double memberWeight;
    private Double memberSmm;
    private Double memberBfm;
    private Double memberBfp;
    private Double memberBmi;
}