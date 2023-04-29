package jpabook.jpashop.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;
import jpabook.jpashop.domain.OrderStatus;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;

import static jpabook.jpashop.domain.QOrder.*;

@Repository
public class OrderRepository {
    private final EntityManager em;
    private final JPAQueryFactory query; //QueryDsl

    public OrderRepository(EntityManager em) {
        this.em = em;
        this.query = new JPAQueryFactory(em);
    }

    public void save(Order order){
        em.persist(order);
    }

    public Order findOne(Long id){
        return em.find(Order.class,id);
    }

    //QueryDsl
    public List<Order> findAll(OrderSearch orderSearch){
        String memberName = orderSearch.getMemberName();
        OrderStatus orderStatus = orderSearch.getOrderStatus();
       List<Order> result = query
                .select(order)
                .from(order)
                .where(orderStatus(orderStatus),likeMemberName(memberName))
                .limit(1000) //setMaxResults
                .fetch();
       return result;
    }

    private BooleanExpression likeMemberName(String memberName){
        if(StringUtils.hasText(memberName)){
            return order.member.name.like("%"+memberName+"%");
        }
        return null;
    }

    private BooleanExpression orderStatus(OrderStatus status){
        if(status != null){
            return order.status.eq(status);
        }
        return null;
    }
    //Join fetch to send query once
    public List<Order> findAllWithMemberDelivery() {
       //xToOne일 경우 distinct 필요 없어
        String jpql = "select o from Order o join fetch o.member m join fetch o.delivery d";
        return em.createQuery(jpql,Order.class)
                .getResultList();
    }
    public List<Order> findAllWithItems() {
        //distinct사용. OneToMany일 경우 데이터가 뻥튀기 되서 distinct 사용
        String jpql = "select distinct o from Order o join fetch o.member m join fetch o.delivery d join fetch o.orderItems oi join fetch oi.item i";
        return em.createQuery(jpql,Order.class)
                .getResultList();
    }

    public List<Order> findAllWithMemberDelivery_page(int offset,int limit) {
        //xToOne일 경우
        String jpql = "select o from Order o join fetch o.member m join fetch o.delivery d";
        return em.createQuery(jpql,Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
}
