package VitAI.injevital.service;

import VitAI.injevital.dto.LoginRequest;
import VitAI.injevital.dto.MemberDTO;
import VitAI.injevital.entity.Member;
import VitAI.injevital.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor

public class MemberService {

    private final MemberRepository memberRepository;
    public void save(MemberDTO memberDTO){
        //repository save 메서드 호출
        Member memberEntity = Member.toMemberEntity(memberDTO);
        memberRepository.save(memberEntity);
    }

    public MemberDTO login(LoginRequest memberDTO){
        Optional<Member> byMemberEmail = memberRepository.findByMemberEmail(memberDTO.getMemberEmail());
        System.out.println(byMemberEmail);
        if(byMemberEmail.isPresent()){
            Member member = byMemberEmail.get();
            if(member.getMemberPassword().equals(memberDTO.getMemberPassword()))
            {
                MemberDTO dto = MemberDTO.toMemberDTO(member);
                return dto;
            }
            else {
                return null;
            }
        }
        else
        {
            return null;
        }

    }

}
