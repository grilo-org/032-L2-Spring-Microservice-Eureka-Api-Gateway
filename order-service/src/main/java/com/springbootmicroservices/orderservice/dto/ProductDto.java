package com.springbootmicroservices.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    
    @NotNull(message = "Height is required")
    @Positive(message = "Height must be greater than 0")
    private Double height;
    
    @NotNull(message = "Width is required")
    @Positive(message = "Width must be greater than 0")
    private Double width;
    
    @NotNull(message = "Length is required")
    @Positive(message = "Length must be greater than 0")
    private Double length;
    
    private String description;
}
