package com.example.petshelter.dto;

import com.example.petshelter.enums.AnimalSpecies;
import com.example.petshelter.enums.HealthStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PetCreateRequest {
    
    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String name;
    
    @NotNull(message = "Species is required")
    private AnimalSpecies species;
    
    @Size(max = 100)
    private String breed;
    
    @Min(value = 0, message = "Age must be positive")
    @Max(value = 30, message = "Age cannot be greater than 30")
    private Integer age;
    
    @Size(max = 50)
    private String color;
    
    @DecimalMin(value = "0.1", message = "Weight must be positive")
    @DecimalMax(value = "100.0", message = "Weight cannot exceed 100kg")
    private Double weightKg;
    
    @NotNull(message = "Arrival date is required")
    private LocalDate arrivalDate;
    
    @NotNull(message = "Health status is required")
    private HealthStatus healthStatus;
    
    @Size(max = 500)
    private String description;
}