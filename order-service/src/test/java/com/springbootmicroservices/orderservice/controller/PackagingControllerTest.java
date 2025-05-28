package com.springbootmicroservices.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springbootmicroservices.orderservice.dto.OrderPackagingResponseDto;
import com.springbootmicroservices.orderservice.dto.OrderRequestDto;
import com.springbootmicroservices.orderservice.dto.PackagingRequestDto;
import com.springbootmicroservices.orderservice.dto.ProductDto;
import com.springbootmicroservices.orderservice.service.PackagingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PackagingController.class)
public class PackagingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PackagingService packagingService;

    @Autowired
    private ObjectMapper objectMapper;

    private PackagingRequestDto packagingRequest;
    private List<OrderPackagingResponseDto> orderResponses;

    @BeforeEach
    void setUp() {
        // Setup test data
        ProductDto product = new ProductDto();
        product.setName("Test Product");
        product.setHeight(10.0);
        product.setWidth(15.0);
        product.setLength(20.0);

        List<ProductDto> products = new ArrayList<>();
        products.add(product);

        OrderRequestDto orderRequest = new OrderRequestDto();
        orderRequest.setOrderNumber("ORD123");
        orderRequest.setProducts(products);

        packagingRequest = new PackagingRequestDto();
        packagingRequest.setOrders(Collections.singletonList(orderRequest));

        // Setup mock response
        OrderPackagingResponseDto responseDto = new OrderPackagingResponseDto();
        responseDto.setOrderNumber("ORD123");
        responseDto.setBoxes(new ArrayList<>());

        orderResponses = Collections.singletonList(responseDto);
    }

    @Test
    void shouldOptimizePackaging() throws Exception {
        // Given
        when(packagingService.packageOrders(anyList())).thenReturn(orderResponses);

        // When & Then
        mockMvc.perform(post("/api/v1/packaging/optimize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(packagingRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders[0].orderNumber").value("ORD123"));
    }

    @Test
    void shouldReturnBadRequestForInvalidInput() throws Exception {
        // Given - invalid request (product with null dimensions)
        ProductDto invalidProduct = new ProductDto();
        invalidProduct.setName("Invalid Product");
        // Width and height are null

        List<ProductDto> products = new ArrayList<>();
        products.add(invalidProduct);

        OrderRequestDto orderRequest = new OrderRequestDto();
        orderRequest.setOrderNumber("ORD123");
        orderRequest.setProducts(products);

        PackagingRequestDto invalidRequest = new PackagingRequestDto();
        invalidRequest.setOrders(Collections.singletonList(orderRequest));

        // When & Then
        mockMvc.perform(post("/api/v1/packaging/optimize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
