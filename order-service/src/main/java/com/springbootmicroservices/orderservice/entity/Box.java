package com.springbootmicroservices.orderservice.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "boxes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Box extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "height", nullable = false)
    private Double height; // in centimeters
    
    @Column(name = "width", nullable = false)
    private Double width; // in centimeters
    
    @Column(name = "length", nullable = false)
    private Double length; // in centimeters;
    
    // Method to calculate volume of the box in cubic centimeters
    public Double getVolume() {
        return height * width * length;
    }
}
