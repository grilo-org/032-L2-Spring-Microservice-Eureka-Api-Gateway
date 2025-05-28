package com.springbootmicroservices.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoxDto {
    private Long id;
    private String name;
    private Double height;
    private Double width;
    private Double length;
    private List<ProductDto> products = new ArrayList<>();
}
