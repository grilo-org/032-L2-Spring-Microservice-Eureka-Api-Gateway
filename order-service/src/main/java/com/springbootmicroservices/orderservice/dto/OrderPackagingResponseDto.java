package com.springbootmicroservices.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPackagingResponseDto {
    private String orderNumber;
    private List<BoxDto> boxes = new ArrayList<>();
}
