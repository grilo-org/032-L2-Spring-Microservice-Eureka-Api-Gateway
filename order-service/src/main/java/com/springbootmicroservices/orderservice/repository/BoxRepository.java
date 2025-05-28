package com.springbootmicroservices.orderservice.repository;

import com.springbootmicroservices.orderservice.entity.Box;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoxRepository extends JpaRepository<Box, Long> {
}
