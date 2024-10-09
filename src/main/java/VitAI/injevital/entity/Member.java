package VitAI.injevital.entity;

import VitAI.injevital.dto.MemberDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Collections;
import java.util.Set;

@Entity
@Builder
@Getter
@Setter
@Table(name = "member_table")
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseEntity{ //table 역할

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //
    private Long id;


    @Column(unique = true)
    private String memberEmail;

    @Column
    private String memberId;

    @Column
    private String memberPassword;

    @Column
    private String memberName;

    @Column
    private String memberHeight;

    @Column
    private String memberWeight;

    @JsonIgnore
    @Column(name = "activated")
    private boolean activated;

    @Column
    private String skeletalMuscleMass;  // 골격근량

    @Column
    private String bodyFatMass;         // 체지방량

    @Column
    private String bodyFatPercentage;   // 체지방률

    @Column
    private String bmi;                 // BMI


    public static Member toMemberEntity(MemberDTO memberDTO){
        return Member.builder()
                .memberWeight(memberDTO.getMemberWeight())
                .memberHeight(memberDTO.getMemberHeight())
                .memberPassword(memberDTO.getMemberPassword())
                .memberName(memberDTO.getMemberName())
                .memberEmail(memberDTO.getMemberEmail())
                .memberId(memberDTO.getMemberId())
                .build();
    }
}
