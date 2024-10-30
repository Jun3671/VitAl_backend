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
public class Member { //table 역할


    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //
    private Long id;

    @Column
    private String memberId;

    @Column

    private String memberPassword;

    @Column
    private String memberName;

    @Column(unique = true)
    private String memberEmail;


    @Column
    private String memberHeight;

    @Column
    protected String memberWeight;

    @Column(name = "activated", nullable = false)
    private boolean activated = true;


    public static Member toMemberEntity(MemberDTO memberDTO){
        return Member.builder()
                .memberId((memberDTO.getMemberId()))
                .memberPassword((memberDTO.getMemberPassword()))
                .memberName(memberDTO.getMemberName())
                .memberEmail(memberDTO.getMemberEmail())
                .memberHeight(memberDTO.getMemberHeight())
                .memberWeight(memberDTO.getMemberWeight())
                .activated(true)
                .build();
    }
}
