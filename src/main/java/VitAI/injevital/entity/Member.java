package VitAI.injevital.entity;

import VitAI.injevital.dto.MemberDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    private String memberId;

    @Column
    private double memberHeight;

    @Column
    private String memberGender;

    @Column
    private double memberWeight;

    @Builder.Default
    private boolean activated = true;;

    @ManyToMany
    @JoinTable(
            name = "user_authority",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "authority_name", referencedColumnName = "authority_name")})
    private Set<Authority> authorities;
    public static Member toMemberEntity(MemberDTO memberDTO , PasswordEncoder passwordEncoder){
        return Member.builder()
                .memberId(memberDTO.getMemberId())
                .memberWeight(memberDTO.getMemberWeight())
                .memberHeight(memberDTO.getMemberHeight())
                .memberPassword(passwordEncoder.encode(memberDTO.getMemberPassword()))
                .memberGender(memberDTO.getMemberGender())
                .memberName(memberDTO.getMemberName())
                .memberEmail(memberDTO.getMemberEmail())
                .activated(true)
                .build();
    }
}
