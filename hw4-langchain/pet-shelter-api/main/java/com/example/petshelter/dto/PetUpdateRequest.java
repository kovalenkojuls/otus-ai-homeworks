package com.example.petshelter.dto;

import com.example.petshelter.enums.HealthStatus;
import com.example.petshelter.enums.PetStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PetUpdateRequest {
    
    @Size(max = 100)
    private String name;
    
    @Size(max = 100)
    private String breed;
    
    private Integer age;
    
    @Size(max = 50)
    private String color;
    
    @DecimalMin(value = "0.1", message = "Weight must be positive")
    @DecimalMax(value = "100.0", message = "Weight cannot exceed 100kg")
    private Double weightKg;
    
    private HealthStatus healthStatus;
    
    @Size(max = 500)
    private String description;
    
    private PetStatus status;
}