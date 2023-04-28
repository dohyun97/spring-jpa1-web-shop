package jpabook.jpashop;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.service.ItemService;
import jpabook.jpashop.service.MemberService;
import jpabook.jpashop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@Component
@Transactional
@RequiredArgsConstructor
public class InitDb {
    private final MemberService memberService;
    private final ItemService itemService;
    private final EntityManager em;

    @EventListener(ApplicationReadyEvent.class)
    public void initData(){
        Member member = createMember("uesrA","Toronto","1","1111");
        memberService.join(member);
        Book book1 = createBook("JPA1 BOOK", 10000, 100);
        itemService.saveItem(book1);
        Book book2 = createBook("JPA2 BOOK",20000,100);
        itemService.saveItem(book2);
        OrderItem orderItem1 = OrderItem.createdOrderItem(book1, 10000, 1);
        OrderItem orderItem2 = OrderItem.createdOrderItem(book2, 20000, 2);
        Order order = Order.createOrder(createDelivery(member), member, orderItem1, orderItem2);
        em.persist(order);

    }
    @EventListener(ApplicationReadyEvent.class)
    public void initData2(){
        Member member = createMember("uesrB","Montreal","2","2222");
        memberService.join(member);
        Book book1 = createBook("SPRING1 BOOK", 20000, 200);
        itemService.saveItem(book1);
        Book book2 = createBook("SPRING2 BOOK",40000,300);
        itemService.saveItem(book2);
        OrderItem orderItem1 = OrderItem.createdOrderItem(book1, 20000, 3);
        OrderItem orderItem2 = OrderItem.createdOrderItem(book2, 40000, 4);
        Order order = Order.createOrder(createDelivery(member), member, orderItem1, orderItem2);
        em.persist(order);
    }
    private Member createMember(String name,String city, String street, String zipcode){
        Member member = new Member();
        member.setName(name);
        member.setAddress(new Address(city,street,zipcode));
        return member;
    }

    private Book createBook(String name,int price,int stockQuantity){
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        return book;
    }

    private Delivery createDelivery(Member member){
        Delivery delivery =new Delivery();
        delivery.setAddress(member.getAddress());
        return delivery;
    }
}
