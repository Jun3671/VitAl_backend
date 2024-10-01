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
    private String memberPassword;

    @Column
    private String memberName;

    @Column
    private String memberNumber;

    @JsonIgnore
    @Column(name = "activated")
    private boolean activated;


    public static Member toMemberEntity(MemberDTO memberDTO){
        return Member.builder()
                .memberNumber(memberDTO.getMemberNumber())
                .memberPassword(memberDTO.getMemberPassword())
                .memberName(memberDTO.getMemberName())
                .memberEmail(memberDTO.getMemberEmail())
                .build();
    }
}
