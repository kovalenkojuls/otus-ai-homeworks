package com.example.petshelter.controller;

import com.example.petshelter.dto.*;
import com.example.petshelter.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @PostMapping
    public ResponseEntity<PetResponse> createPet(@Valid @RequestBody PetCreateRequest request) {
        PetResponse pet = petService.createPet(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(pet);
    }

    @GetMapping
    public ResponseEntity<List<PetResponse>> getAllPets(
            @RequestParam(required = false) String species,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false, defaultValue = "arrival_date") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection) {

        PetFilterRequest filter = new PetFilterRequest();
        filter.setSpecies(species);
        filter.setStatus(status);
        filter.setMaxAge(maxAge);
        filter.setSortBy(sortBy);
        filter.setSortDirection(sortDirection);

        List<PetResponse> pets = petService.getAllPets(filter);
        return ResponseEntity.ok(pets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PetResponse> getPetById(@PathVariable Long id) {
        PetResponse pet = petService.getPetById(id);
        return pet != null ? ResponseEntity.ok(pet) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<PetResponse> updatePet(
            @PathVariable Long id,
            @Valid @RequestBody PetUpdateRequest request) {
        PetResponse pet = petService.updatePet(id, request);
        return pet != null ? ResponseEntity.ok(pet) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePet(@PathVariable Long id) {
        boolean deleted = petService.deletePet(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}