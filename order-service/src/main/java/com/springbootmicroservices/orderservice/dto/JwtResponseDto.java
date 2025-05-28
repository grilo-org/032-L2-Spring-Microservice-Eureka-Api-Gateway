package com.springbootmicroservices.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponseDto {
    private String token;
    private String username;
    private String name;
    private String type = "Bearer";
}
