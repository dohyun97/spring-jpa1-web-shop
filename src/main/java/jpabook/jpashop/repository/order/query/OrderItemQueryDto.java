package jpabook.jpashop.repository.order.query;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jpabook.jpashop.domain.OrderItem;
import lombok.Data;

@Data
public class OrderItemQueryDto {
    @JsonIgnore
    private Long id;
    private String itemName;
    private int orderPrice;
    private int count;
    public OrderItemQueryDto(Long orderId,String itemName,int orderPrice,int count){
        this.id = orderId;
        this.itemName = itemName;
        this.orderPrice = orderPrice;
        this.count = count;
    }
}
