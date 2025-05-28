package com.springbootmicroservices.apigateway.config;

import com.springbootmicroservices.apigateway.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private static final String ORDER_SERVICE_URI = "lb://order-service";

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("order_service_route", r -> r.path("/order-service/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                    .rewritePath("/order-service/(?<segment>.*)", "/${segment}"))
                        .uri(ORDER_SERVICE_URI))
                .route("swagger_route", r -> r.path("/swagger-api/**")
                        .filters(f -> f.rewritePath("/swagger-api/(?<segment>.*)", "/swagger-ui/${segment}"))
                        .uri(ORDER_SERVICE_URI))
                .route("api_docs_route", r -> r.path("/v3/api-docs/**")
                        .uri(ORDER_SERVICE_URI))
                .build();
    }
}
