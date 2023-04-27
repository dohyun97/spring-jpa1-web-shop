package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class Delivery {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Long id;

    @Embedded
    private Address address;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "delivery")
    private Order order;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

}
