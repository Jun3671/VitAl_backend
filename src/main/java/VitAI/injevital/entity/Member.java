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
    private String memberId;

    @Column
    private String memberPassword;

    @Column
    private String memberName;

    @Column
    private String memberNumber;

    @Column(name = "activated")
    private boolean activated = true;

    @ManyToMany
    @JoinTable(
            name = "user_authority",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "authority_name", referencedColumnName = "authority_name")})
    private Set<Authority> authorities;
    public static Member toMemberEntity(MemberDTO memberDTO , Authority authority , PasswordEncoder passwordEncoder){
        return Member.builder()
                .authorities(Collections.singleton(authority))
                .memberNumber(memberDTO.getMemberNumber())
                .memberPassword(passwordEncoder.encode(memberDTO.getMemberPassword()))
                .memberName(memberDTO.getMemberName())
                .memberEmail(memberDTO.getMemberEmail())
                .memberId(memberDTO.getMemberId())
                .build();
    }
}
