package com.springbootmicroservices.orderservice.repository;

import com.springbootmicroservices.orderservice.entity.Order;
import com.springbootmicroservices.orderservice.entity.OrderPackaging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderPackagingRepository extends JpaRepository<OrderPackaging, Long> {
    List<OrderPackaging> findByOrder(Order order);
    void deleteByOrder(Order order);
}
