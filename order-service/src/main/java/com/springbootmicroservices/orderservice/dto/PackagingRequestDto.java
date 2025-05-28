package com.springbootmicroservices.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackagingRequestDto {
    @Valid
    private List<OrderRequestDto> orders = new ArrayList<>();
}
