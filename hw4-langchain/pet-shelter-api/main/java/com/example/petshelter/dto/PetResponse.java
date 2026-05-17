package com.example.petshelter.dto;

import com.example.petshelter.enums.AnimalSpecies;
import com.example.petshelter.enums.HealthStatus;
import com.example.petshelter.enums.PetStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PetResponse {
    private Long id;
    private String name;
    private AnimalSpecies species;
    private String breed;
    private Integer age;
    private String color;
    private Double weightKg;
    private LocalDate arrivalDate;
    private HealthStatus healthStatus;
    private String description;
    private PetStatus status;
    private String statusLabel;
    
    public static PetResponse fromEntity(com.example.petshelter.entity.Pet pet) {
        PetResponse response = new PetResponse();
        response.setId(pet.getId());
        response.setName(pet.getName());
        response.setSpecies(pet.getSpecies());
        response.setBreed(pet.getBreed());
        response.setAge(pet.getAge());
        response.setColor(pet.getColor());
        response.setWeightKg(pet.getWeightKg());
        response.setArrivalDate(pet.getArrivalDate());
        response.setHealthStatus(pet.getHealthStatus());
        response.setDescription(pet.getDescription());
        response.setStatus(pet.getStatus());
        response.setStatusLabel(pet.getStatus().getValue());
        return response;
    }
}