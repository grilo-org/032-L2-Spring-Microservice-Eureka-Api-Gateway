package com.springbootmicroservices.orderservice.service;

import com.springbootmicroservices.orderservice.dto.BoxDto;
import com.springbootmicroservices.orderservice.dto.OrderPackagingResponseDto;
import com.springbootmicroservices.orderservice.dto.OrderRequestDto;
import com.springbootmicroservices.orderservice.dto.ProductDto;

import java.util.List;

public interface PackagingService {
    List<OrderPackagingResponseDto> packageOrders(List<OrderRequestDto> orderRequests);
    List<BoxDto> findOptimalPackaging(List<ProductDto> products);
}
