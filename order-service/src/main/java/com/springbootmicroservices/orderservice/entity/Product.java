package com.springbootmicroservices.orderservice.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "height", nullable = false)
    @NotNull
    @Positive
    private Double height; // in centimeters
    
    @Column(name = "width", nullable = false)
    @NotNull
    @Positive
    private Double width; // in centimeters
    
    @Column(name = "length", nullable = false)
    @NotNull
    @Positive
    private Double length; // in centimeters
    
    @Column(name = "description")
    private String description;
    
    // Method to calculate volume of the product in cubic centimeters
    public Double getVolume() {
        return height * width * length;
    }
}
