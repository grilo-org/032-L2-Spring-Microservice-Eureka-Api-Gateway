package com.springbootmicroservices.orderservice.service;

import com.springbootmicroservices.orderservice.dto.BoxDto;
import com.springbootmicroservices.orderservice.dto.OrderRequestDto;
import com.springbootmicroservices.orderservice.dto.ProductDto;
import com.springbootmicroservices.orderservice.entity.Box;
import com.springbootmicroservices.orderservice.repository.BoxRepository;
import com.springbootmicroservices.orderservice.repository.OrderPackagingRepository;
import com.springbootmicroservices.orderservice.repository.OrderRepository;
import com.springbootmicroservices.orderservice.service.impl.PackagingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PackagingServiceTest {

    @Mock
    private BoxRepository boxRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderPackagingRepository orderPackagingRepository;

    @InjectMocks
    private PackagingServiceImpl packagingService;

    private List<Box> sampleBoxes;
    private List<ProductDto> sampleProducts;

    @BeforeEach
    void setUp() {
        // Sample boxes
        Box box1 = new Box();
        box1.setId(1L);
        box1.setName("Caixa 1");
        box1.setHeight(30.0);
        box1.setWidth(40.0);
        box1.setLength(80.0);

        Box box2 = new Box();
        box2.setId(2L);
        box2.setName("Caixa 2");
        box2.setHeight(80.0);
        box2.setWidth(50.0);
        box2.setLength(40.0);

        Box box3 = new Box();
        box3.setId(3L);
        box3.setName("Caixa 3");
        box3.setHeight(50.0);
        box3.setWidth(80.0);
        box3.setLength(60.0);

        sampleBoxes = Arrays.asList(box1, box2, box3);

        // Sample products
        sampleProducts = new ArrayList<>();
        
        ProductDto product1 = new ProductDto();
        product1.setName("Small Product");
        product1.setHeight(10.0);
        product1.setWidth(15.0);
        product1.setLength(20.0);
        sampleProducts.add(product1);
        
        ProductDto product2 = new ProductDto();
        product2.setName("Medium Product");
        product2.setHeight(25.0);
        product2.setWidth(30.0);
        product2.setLength(35.0);
        sampleProducts.add(product2);

        when(boxRepository.findAll()).thenReturn(sampleBoxes);
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldFindOptimalPackaging() {
        // Given
        when(boxRepository.save(any(Box.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        List<BoxDto> result = packagingService.findOptimalPackaging(sampleProducts);
        
        // Then
        assertNotNull(result);
        // We expect a single box can contain both products
        assertEquals(1, result.size(), "Should return a single box for these small products");
        assertEquals(2, result.get(0).getProducts().size(), "The box should contain both products");
    }

    @Test
    void shouldUseMultipleBoxesWhenNeeded() {
        // Given
        ProductDto largeProduct = new ProductDto();
        largeProduct.setName("Large Product");
        largeProduct.setHeight(45.0);
        largeProduct.setWidth(70.0);
        largeProduct.setLength(55.0);
        sampleProducts.add(largeProduct);
        
        when(boxRepository.save(any(Box.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        List<BoxDto> result = packagingService.findOptimalPackaging(sampleProducts);
        
        // Then
        assertNotNull(result);
        // The large product might need its own box
        assertTrue(result.size() >= 1, "Should use appropriate number of boxes");
        
        // Verify the total number of products across all boxes
        int totalProducts = result.stream()
                .mapToInt(box -> box.getProducts().size())
                .sum();
        
        assertEquals(3, totalProducts, "All products should be assigned to boxes");
    }

    @Test
    void shouldHandleEmptyProductList() {
        // Given
        List<ProductDto> emptyProducts = new ArrayList<>();
        
        // When
        List<BoxDto> result = packagingService.findOptimalPackaging(emptyProducts);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Result should be an empty list for no products");
    }
}
