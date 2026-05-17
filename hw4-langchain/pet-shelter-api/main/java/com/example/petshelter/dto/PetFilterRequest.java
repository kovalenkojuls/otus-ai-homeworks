package com.example.petshelter.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PetFilterRequest {
    private String species; // AnimalSpecies
    private String status; // PetStatus
    private Integer maxAge;
    private String sortBy; // arrival_date, age, weight_kg, name
    private String sortDirection; // asc, desc
}