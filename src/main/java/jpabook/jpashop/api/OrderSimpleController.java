package jpabook.jpashop.api;

import jpabook.jpashop.domain.*;

import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import jpabook.jpashop.service.OrderService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/** *
 * xToOne(ManyToOne, OneToOne) 관계 최적화 * Order
 * Order -> Member  //ManyToOne
 * Order -> Delivery  //OneToMany
 *
 *items 부분은 이쪽 연관관계가 아닌데 내가 그냥 해봤어(ordrAPIController)에서 자세히 설명..
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleController {
    private final OrderService service;
    private final OrderSimpleQueryRepository queryRepository;

    /**
     * V2. 엔티티를 조회해서 DTO로 변환(fetch join 사용X) * - 단점: 지연로딩으로 쿼리 N번 호출,On
     */
    @GetMapping("/api/v2/simple-orders")
    public Result orderV2(){
        List<Order> orders = service.findOrders(new OrderSearch());
        List<SimpleOrderDto> collect = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
        return new Result(collect);
    }

    /**
     * V3. 엔티티를 조회해서 DTO로 변환(fetch join 사용O)
     * - fetch join으로 쿼리 1번 호출
     * 참고: fetch join에 대한 자세한 내용은 JPA 기본편 참고(정말 중요함) */
    @GetMapping("/api/v3/simple-orders")
    public Result orderV3(){
        List<Order> orders = service.findOrdersWithMemberDelivery();
        List<SimpleOrderDto> collect = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
        return new Result(collect);
    }

    /**
     * V4. JPA에서 DTO로 바로 조회(Repository)
     *-쿼리1번 호출
     * - select 절에서 원하는 데이터만 선택해서 조회 */
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4(){
        return queryRepository.findOrderDtos();
    }

    @Data
    @AllArgsConstructor
    static class Result<T>{
        private T data;
    }

    @Data
    static class SimpleOrderDto{
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private ItemResult orderItems;
        public SimpleOrderDto(Order order){
            orderId = order.getId();
            name =order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems= getSingleItemName(order);
        }
        //이부분은 items를 위한 부분 내가 그냥 한거야
        private ItemResult getSingleItemName(Order order){
            List<String> items = order.getOrderItems().stream()
                    .map(i->i.getItem().getName())
                    .collect(Collectors.toList());
            return new ItemResult(items);
        }
    }
    @Data   //이부분은 items를 위한 부분 내가 그냥한거야
    @AllArgsConstructor
    static class ItemResult<T>{
        private T item;
    }

}
