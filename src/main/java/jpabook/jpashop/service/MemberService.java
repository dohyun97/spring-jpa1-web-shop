package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository repository;

    @Transactional
    public Long join(Member member){
        validateDuplicateMember(member);
        repository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        if(! repository.findByName(member.getName()).isEmpty()){
            throw new IllegalStateException("Member already exists");
        }
    }

    public List<Member> findMembers(){
        return repository.findAll();
    }

    public Member findOne(Long memberId){
        return repository.findOne(memberId);
    }
}
