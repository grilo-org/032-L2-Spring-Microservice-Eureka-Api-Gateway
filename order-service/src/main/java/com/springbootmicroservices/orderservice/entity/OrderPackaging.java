package com.springbootmicroservices.orderservice.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_packaging")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderPackaging extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne
    @JoinColumn(name = "box_id", nullable = false)
    private Box box;
    
    @ManyToMany
    @JoinTable(
        name = "packaging_products",
        joinColumns = @JoinColumn(name = "packaging_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products = new ArrayList<>();
}
