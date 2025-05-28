package com.springbootmicroservices.orderservice.service.impl;

import com.springbootmicroservices.orderservice.dto.*;
import com.springbootmicroservices.orderservice.entity.Box;
import com.springbootmicroservices.orderservice.entity.Order;
import com.springbootmicroservices.orderservice.entity.OrderPackaging;
import com.springbootmicroservices.orderservice.entity.Product;
import com.springbootmicroservices.orderservice.repository.BoxRepository;
import com.springbootmicroservices.orderservice.repository.OrderPackagingRepository;
import com.springbootmicroservices.orderservice.repository.OrderRepository;
import com.springbootmicroservices.orderservice.service.PackagingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PackagingServiceImpl implements PackagingService {

    private final BoxRepository boxRepository;
    private final OrderRepository orderRepository;
    private final OrderPackagingRepository orderPackagingRepository;

    // Initialize the available boxes as specified in the requirements
    @PostConstruct
    public void initializeBoxes() {
        // Check if boxes are already initialized
        if (boxRepository.count() > 0) {
            return;
        }
        
        List<Box> boxes = new ArrayList<>();
        
        // Caixa 1: 30 x 40 x 80 cm
        Box box1 = new Box();
        box1.setName("Caixa 1");
        box1.setHeight(30.0);
        box1.setWidth(40.0);
        box1.setLength(80.0);
        boxes.add(box1);
        
        // Caixa 2: 80 x 50 x 40 cm
        Box box2 = new Box();
        box2.setName("Caixa 2");
        box2.setHeight(80.0);
        box2.setWidth(50.0);
        box2.setLength(40.0);
        boxes.add(box2);
        
        // Caixa 3: 50 x 80 x 60 cm
        Box box3 = new Box();
        box3.setName("Caixa 3");
        box3.setHeight(50.0);
        box3.setWidth(80.0);
        box3.setLength(60.0);
        boxes.add(box3);
        
        boxRepository.saveAll(boxes);
    }

    @Override
    @Transactional
    public List<OrderPackagingResponseDto> packageOrders(List<OrderRequestDto> orderRequests) {
        List<OrderPackagingResponseDto> responseList = new ArrayList<>();
        
        for (OrderRequestDto orderRequest : orderRequests) {
            // Create and save order
            Order order = new Order();
            order.setOrderNumber(orderRequest.getOrderNumber() != null ? 
                    orderRequest.getOrderNumber() : UUID.randomUUID().toString());
            
            // Convert DTO to entity
            List<Product> products = orderRequest.getProducts().stream()
                    .map(this::mapToProduct)
                    .collect(Collectors.toList());
            
            order.setProducts(products);
            Order savedOrder = orderRepository.save(order);
            
            // Find optimal packaging
            List<BoxDto> optimalBoxes = findOptimalPackaging(orderRequest.getProducts());
            
            // Save packaging details
            saveOrderPackaging(savedOrder, optimalBoxes);
            
            // Create response
            OrderPackagingResponseDto responseDto = new OrderPackagingResponseDto();
            responseDto.setOrderNumber(savedOrder.getOrderNumber());
            responseDto.setBoxes(optimalBoxes);
            
            responseList.add(responseDto);
        }
        
        return responseList;
    }

    @Override
    public List<BoxDto> findOptimalPackaging(List<ProductDto> productDtos) {
        // Get all available box types
        List<Box> availableBoxes = boxRepository.findAll();
        
        if (availableBoxes.isEmpty()) {
            throw new RuntimeException("No box configurations are available");
        }
        
        // Convert products to a more usable format for the algorithm
        List<Product> products = productDtos.stream()
                .map(this::mapToProduct)
                .sorted(Comparator.comparing(Product::getVolume).reversed())
                .collect(Collectors.toList());
        
        // First-Fit Decreasing bin packing algorithm
        List<BoxDto> resultBoxes = new ArrayList<>();
        List<List<Product>> boxContents = new ArrayList<>();
        
        // Try to place each product
        for (Product product : products) {
            boolean placed = false;
            
            // Try to place the product in an existing box
            for (int i = 0; i < boxContents.size(); i++) {
                Box boxType = mapToBox(resultBoxes.get(i));
                List<Product> currentBoxProducts = boxContents.get(i);
                
                // Check if product can fit in this box by volume
                double currentVolume = currentBoxProducts.stream()
                        .mapToDouble(Product::getVolume)
                        .sum();
                
                if (currentVolume + product.getVolume() <= boxType.getVolume() && 
                        canFitPhysically(product, boxType, currentBoxProducts)) {
                    // Product fits in this box
                    currentBoxProducts.add(product);
                    resultBoxes.get(i).getProducts().add(mapToProductDto(product));
                    placed = true;
                    break;
                }
            }
            
            // If product couldn't fit in any existing box, create a new box
            if (!placed) {
                // Find the smallest box that can fit the product
                Box bestBox = findBestBoxForProduct(product, availableBoxes);
                
                BoxDto newBoxDto = mapToBoxDto(bestBox);
                newBoxDto.getProducts().add(mapToProductDto(product));
                resultBoxes.add(newBoxDto);
                
                List<Product> newBoxContents = new ArrayList<>();
                newBoxContents.add(product);
                boxContents.add(newBoxContents);
            }
        }
        
        return resultBoxes;
    }

    private boolean canFitPhysically(Product product, Box box, List<Product> existingProducts) {
        // Verifica múltiplas orientações do produto (6 possíveis rotações) 
        if (canRotateAndFit(product, box, existingProducts)) {
            return true;
        }
        
        // Verificação baseada apenas no volume como fallback
        double totalVolume = existingProducts.stream()
                .mapToDouble(Product::getVolume)
                .sum() + product.getVolume();
                
        return totalVolume <= box.getVolume();
    }
      private boolean canRotateAndFit(Product product, Box box, List<Product> existingProducts) {
        double totalExistingVolume = existingProducts.stream()
                .mapToDouble(Product::getVolume)
                .sum();
                
        if (totalExistingVolume + product.getVolume() > box.getVolume()) {
            return false; // Falha imediata se o volume total exceder
        }
        
        // Verificar espaço disponível na caixa após produtos existentes
        double occupiedSpace = calculateOccupiedSpace(existingProducts);
        double availableSpace = box.getVolume() - occupiedSpace;
        
        if (product.getVolume() > availableSpace) {
            return false; // Não há espaço suficiente
        }
        
        // Testar todas as 6 possíveis orientações
        return
            // Orientação original
            (fitsInBox(product.getHeight(), product.getWidth(), product.getLength(), box)) ||
            // Rotação 90° no eixo Y
            (fitsInBox(product.getHeight(), product.getLength(), product.getWidth(), box)) ||
            // Rotação 90° no eixo X
            (fitsInBox(product.getWidth(), product.getHeight(), product.getLength(), box)) ||
            // Rotação 90° em X, 90° em Y
            (fitsInBox(product.getWidth(), product.getLength(), product.getHeight(), box)) ||
            // Rotação 90° em Z
            (fitsInBox(product.getLength(), product.getHeight(), product.getWidth(), box)) ||
            // Rotação 90° em Z, 90° em Y
            (fitsInBox(product.getLength(), product.getWidth(), product.getHeight(), box));
    }
    
    // Calcular o espaço ocupado com maior precisão, considerando a distribuição espacial
    private double calculateOccupiedSpace(List<Product> products) {
        if (products.isEmpty()) {
            return 0.0;
        }
        
        // Cálculo básico do volume total
        return products.stream()
                .mapToDouble(Product::getVolume)
                .sum();
        
        // Nota: Uma implementação mais avançada incluiria algoritmos 
        // de empacotamento 3D para calcular o espaço real ocupado
        // considerando como as peças se encaixam
    }
    
    private boolean fitsInBox(double height, double width, double length, Box box) {
        return height <= box.getHeight() && width <= box.getWidth() && length <= box.getLength();
    }    private Box findBestBoxForProduct(Product product, List<Box> availableBoxes) {
        return availableBoxes.stream()
                .filter(box -> 
                    // Verifica todas as possíveis orientações do produto
                    (product.getHeight() <= box.getHeight() && product.getWidth() <= box.getWidth() && product.getLength() <= box.getLength()) ||
                    (product.getHeight() <= box.getHeight() && product.getLength() <= box.getWidth() && product.getWidth() <= box.getLength()) ||
                    (product.getWidth() <= box.getHeight() && product.getHeight() <= box.getWidth() && product.getLength() <= box.getLength()) ||
                    (product.getWidth() <= box.getHeight() && product.getLength() <= box.getWidth() && product.getHeight() <= box.getLength()) ||
                    (product.getLength() <= box.getHeight() && product.getHeight() <= box.getWidth() && product.getWidth() <= box.getLength()) ||
                    (product.getLength() <= box.getHeight() && product.getWidth() <= box.getWidth() && product.getHeight() <= box.getLength())
                )
                .min(Comparator.comparing(Box::getVolume))
                .orElseThrow(() -> new RuntimeException("No box can fit this product: " + product.getName() + 
                    " with dimensions (H×W×L): " + product.getHeight() + "×" + product.getWidth() + "×" + product.getLength()));
    }

    private void saveOrderPackaging(Order order, List<BoxDto> boxDtos) {
        // First delete any existing packaging for this order
        orderPackagingRepository.deleteByOrder(order);
        
        for (BoxDto boxDto : boxDtos) {
            Box box = mapToBox(boxDto);
            
            OrderPackaging packaging = new OrderPackaging();
            packaging.setOrder(order);
            packaging.setBox(box);
            
            List<Product> productsInBox = boxDto.getProducts().stream()
                    .map(this::mapToProduct)
                    .collect(Collectors.toList());
            
            // Find actual product entities from the order
            List<Product> orderProducts = order.getProducts();
            List<Product> matchedProducts = new ArrayList<>();
            
            for (Product boxProduct : productsInBox) {
                // Find matching product from order
                Product matchedProduct = orderProducts.stream()
                        .filter(p -> Objects.equals(p.getId(), boxProduct.getId()) || 
                                    (p.getHeight().equals(boxProduct.getHeight()) && 
                                     p.getWidth().equals(boxProduct.getWidth()) &&
                                     p.getLength().equals(boxProduct.getLength())))
                        .findFirst()
                        .orElse(boxProduct); // Fallback to the box product if no match
                        
                matchedProducts.add(matchedProduct);
            }
            
            packaging.setProducts(matchedProducts);
            orderPackagingRepository.save(packaging);
        }
    }

    // Helper methods for object conversion
    private Product mapToProduct(ProductDto dto) {
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName() != null ? dto.getName() : "Product");
        product.setHeight(dto.getHeight());
        product.setWidth(dto.getWidth());
        product.setLength(dto.getLength());
        product.setDescription(dto.getDescription());
        return product;
    }

    private ProductDto mapToProductDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getHeight(),
                product.getWidth(),
                product.getLength(),
                product.getDescription()
        );
    }

    private BoxDto mapToBoxDto(Box box) {
        return new BoxDto(
                box.getId(),
                box.getName(),
                box.getHeight(),
                box.getWidth(),
                box.getLength(),
                new ArrayList<>()
        );
    }

    private Box mapToBox(BoxDto dto) {
        // If it's an existing box (has ID), find it
        if (dto.getId() != null) {
            return boxRepository.findById(dto.getId()).orElseGet(() -> {
                Box newBox = new Box();
                newBox.setName(dto.getName());
                newBox.setHeight(dto.getHeight());
                newBox.setWidth(dto.getWidth());
                newBox.setLength(dto.getLength());
                return boxRepository.save(newBox);
            });
        }
        
        // Otherwise create a new one
        Box box = new Box();
        box.setName(dto.getName());
        box.setHeight(dto.getHeight());
        box.setWidth(dto.getWidth());
        box.setLength(dto.getLength());
        return boxRepository.save(box);
    }
}
