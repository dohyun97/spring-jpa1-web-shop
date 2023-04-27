package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@Transactional
@SpringBootTest
class OrderServiceTest {
    @Autowired
    OrderService orderService;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    OrderRepository orderRepository;

    @Test
    void order(){
        //given
        Member member = createMember();
        Item item = createBook("JPA Book", 100, 10);
        int orderCount = 2;
        //then
        Long orderId = orderService.order(item.getId(), member.getId(), orderCount);
        //when
        Order findOrder = orderRepository.findOne(orderId);
        assertThat(findOrder.getStatus()).isEqualTo(OrderStatus.ORDER);
        assertThat(findOrder.getTotalPrice()).isEqualTo(200);
        assertThat(findOrder.getOrderItems().size()).isEqualTo(1);
        assertThat(item.getStockQuantity()).isEqualTo(8);
    }

    @Test
    void overStockQuantity(){
        //Given
        Member member = createMember();
        Item item = createBook("JPA Book", 100, 10);
        int orderCount = 12;
        //then //when
       assertThatThrownBy(()->orderService.order(item.getId(),member.getId(),orderCount)).isInstanceOf(NotEnoughStockException.class);
    }

    @Test
    void cancelOrder(){
        //given
        Member member = createMember();
        Item item = createBook("JPA Book", 100, 10);
        int orderCount = 2;
        Long orderId = orderService.order(item.getId(), member.getId(), orderCount);
        //when
        orderService.cancelOrder(orderId);
        //then
        Order findOrder = orderRepository.findOne(orderId);
        assertThat(findOrder.getStatus()).isEqualTo(OrderStatus.CANCEL);
        assertThat(item.getStockQuantity()).isEqualTo(10);
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("member1");
        member.setAddress(new Address("Toronto","Yonge","M3M000"));
        memberRepository.save(member);
        return member;
    }

    private Book createBook(String name,int price,int stockQuantity){
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        itemRepository.save(book);
        return book;
    }

}