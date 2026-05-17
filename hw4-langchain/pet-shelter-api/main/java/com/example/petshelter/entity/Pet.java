package com.example.petshelter.entity;

import com.example.petshelter.enums.AnimalSpecies;
import com.example.petshelter.enums.HealthStatus;
import com.example.petshelter.enums.PetStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
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
    @Column(name = "arrival_date")
    private LocalDate arrivalDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "health_status", nullable = false)
    private HealthStatus healthStatus;
    
    @Size(max = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PetStatus status;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = PetStatus.AVAILABLE;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}