package VitAI.injevital.entity;

import VitAI.injevital.dto.MemberDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

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


    @Column
    private String memberGender;

    @Column(unique = true)
    private String memberEmail;

    @Column
    private String memberId;

    @Column
    private String memberPassword;

    @Column
    private String memberName;

    @Column
    private String memberNumber;

    @Column
    private String memberHeight;

    @Column
    private String memberWeight;


    @JsonIgnore
    @Column(name = "activated")
    private boolean activated;


    public static Member toMemberEntity(MemberDTO memberDTO, PasswordEncoder passwordEncoder){
        return Member.builder()
                .memberNumber(memberDTO.getMemberNumber())
                .memberPassword(memberDTO.getMemberPassword())
                .memberName(memberDTO.getMemberName())
                .memberEmail(memberDTO.getMemberEmail())
                .memberId(memberDTO.getMemberId())
                .memberHeight(memberDTO.getMemberHeight())
                .memberWeight(memberDTO.getMemberWeight())
                .memberGender(memberDTO.getMemberGender())
                .build();
    }
}
