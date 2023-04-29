package jpabook.jpashop.api;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import jpabook.jpashop.service.OrderService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

/**
 * xToOne(ManyToOne, OneToOne) and OneToMany 관계 최적화 (List<>orderItems추가)
 * V1. 엔티티 직접 노출
 * - 엔티티가 변하면 API 스펙이 변한다.
 * - 트랜잭션 안에서 지연 로딩 필요
 * - 양방향 연관관계 문제 *
 * V2. 엔티티를 조회해서 DTO로 변환(fetch join 사용X)
 * - 트랜잭션 안에서 지연 로딩 필요
 * V3. 엔티티를 조회해서 DTO로 변환(fetch join 사용O)
 * - 페이징 시에는 N 부분을 포기해야함(대신에 batch fetch size? 옵션 주면 N -> 1 쿼리로 변경
 가능) *
 *V4.JPA에서 DTO로 바로 조회, 컬렉션 N 조회 (1+NQuery)
 * - 페이징 가능
 *V5.JPA에서 DTO로 바로 조회, 컬렉션 1 조회 최적화 버전 (1+1Query)
 * - 페이징 가능
 * V6. JPA에서 DTO로 바로 조회, 플랫 데이터(1Query) (1 Query)
 * - 페이징 불가능... *
 */
@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderService service;
    private final OrderQueryRepository orderQueryRepository;

    /**
     * V2. 엔티티를 조회해서 DTO로 변환(fetch join 사용X) * - 단점: 지연로딩으로 쿼리 N번 호출
     */
    @GetMapping("/api/v2/orders")
    public Result orderV2(){
        List<Order> orders = service.findOrders(new OrderSearch());
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return new Result(collect);
    }

    /**
     * V3. 엔티티를 조회해서 DTO로 변환(fetch join 사용O)
     * - fetch join으로 쿼리 1번 호출
     * 참고: fetch join에 대한 자세한 내용은 JPA 기본편 참고(정말 중요함)
     * 단점: OneToMany일 경우에 페이징 불가능.(OneToOne,ManyToOne은 가능)
     * */
    @GetMapping("/api/v3/orders")
    public Result orderV3(){
        List<Order> orders = service.findOrdersWithItems();
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return new Result(collect);
    }

    /**
     * paging 한계 돌파
     * V3.1 엔티티를 조회해서 DTO로 변환 페이징 고려
     *-ToOne 관계만 우선 모두 페치 조인으로 최적화
     * - 컬렉션 관계는 hibernate.default_batch_fetch_size, @BatchSize로 최적화: query 총 3번 날려
     * */
    @GetMapping("/api/v3.1/orders")
    public Result orderV3_page(@RequestParam(defaultValue = "0") int offset,@RequestParam(defaultValue = "100") int limit){
        List<Order> orders = service.findOrdersWithMemberDelivery_page(offset,limit);
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return new Result(collect);
    }

    /**
     * Jpa에서 DTO 직접 조회 -쿼리 3번 호출: 루트 1번, 컬렉션 2번(2개의 주문 아이디가 있어서)
     */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }
    /**
     * Jpa에서 DTO 직접 조회 -쿼리 3번 호출: 루트 1번, 컬렉션 1번(in절 사용)
     */
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    /**
     * Jpa에서 DTO 직접 조회 -쿼리 1번 호출
     */
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();
        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), toList())))
                .entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
                .collect(toList());
    }



    @Data
    @AllArgsConstructor
    static class Result<T>{
        private T data;
    }
    @Data
    static class OrderDto{
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;
        public OrderDto(Order order){
            orderId = order.getId();
            name =order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems= order.getOrderItems().stream()
                    .map(i -> new OrderItemDto(i))
                    .collect(toList());
        }

    }

    @Data
    static class OrderItemDto{
        private String itemName;
        private int orderPrice;
        private int count;
        public OrderItemDto(OrderItem orderItem){
            itemName=orderItem.getItem().getName();
            orderPrice=orderItem.getOrderPrice();
            count=orderItem.getCount();
        }
    }
}
