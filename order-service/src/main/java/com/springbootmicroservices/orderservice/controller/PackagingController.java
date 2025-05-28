package com.springbootmicroservices.orderservice.controller;

import com.springbootmicroservices.orderservice.dto.OrderPackagingResponseDto;
import com.springbootmicroservices.orderservice.dto.PackagingRequestDto;
import com.springbootmicroservices.orderservice.dto.PackagingResponseDto;
import com.springbootmicroservices.orderservice.service.PackagingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/packaging")
@RequiredArgsConstructor
@Tag(name = "Package Optimization API", description = "API for optimizing order packaging in boxes")
public class PackagingController {

    private final PackagingService packagingService;

    @PostMapping("/optimize")
    @Operation(
        summary = "Optimize packaging for orders",
        description = "Receives a list of orders with products and returns the optimal packaging configuration",
        security = { @SecurityRequirement(name = "bearer-jwt") },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Orders packaged successfully",
                content = @Content(schema = @Schema(implementation = PackagingResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Server error")
        }
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<PackagingResponseDto> optimizePackaging(
            @Valid @RequestBody PackagingRequestDto request) {
        
        List<OrderPackagingResponseDto> orderResponses = 
                packagingService.packageOrders(request.getOrders());
        
        PackagingResponseDto response = new PackagingResponseDto();
        response.setOrders(orderResponses);
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
