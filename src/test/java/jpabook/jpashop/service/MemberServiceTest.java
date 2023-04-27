package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Transactional
class MemberServiceTest {
    @Autowired
    MemberRepository repository;
    @Autowired
    MemberService service;

    @Test
    void signUp(){
        //Given
        Member member = new Member();
        member.setName("Kim");
        //When
        Long saveId = service.join(member);
        //then
        assertThat(saveId).isEqualTo(member.getId());
    }

    @Test
    void duplicateException(){
        //Given
        Member member1 = new Member();
        member1.setName("Kim");
        Member member2 = new Member();
        member2.setName("Kim");
        //When
        service.join(member1);

        //then
        assertThatThrownBy(()->service.join(member2)).isInstanceOf(IllegalStateException.class);
    }

}