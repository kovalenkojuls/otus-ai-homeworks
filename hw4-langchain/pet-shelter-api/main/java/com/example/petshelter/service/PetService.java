package com.example.petshelter.service;

import com.example.petshelter.dto.*;
import com.example.petshelter.entity.Pet;
import com.example.petshelter.enums.AnimalSpecies;
import com.example.petshelter.enums.PetStatus;
import com.example.petshelter.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PetService {

    private final PetRepository petRepository;

    public PetResponse createPet(PetCreateRequest request) {
        Pet pet = new Pet();
        pet.setName(request.getName());
        pet.setSpecies(request.getSpecies());
        pet.setBreed(request.getBreed());
        pet.setAge(request.getAge());
        pet.setColor(request.getColor());
        pet.setWeightKg(request.getWeightKg());
        pet.setArrivalDate(request.getArrivalDate());
        pet.setHealthStatus(request.getHealthStatus());
        pet.setDescription(request.getDescription());
        pet.setStatus(PetStatus.AVAILABLE);

        pet = petRepository.save(pet);

        return PetResponse.fromEntity(pet);
    }

    @Transactional(readOnly = true)
    public List<PetResponse> getAllPets(PetFilterRequest filter) {
        AnimalSpecies speciesEnum = null;
        if (filter.getSpecies() != null && !filter.getSpecies().isEmpty()) {
            try {
                speciesEnum = AnimalSpecies.fromValue(filter.getSpecies().toLowerCase());
            } catch (IllegalArgumentException e) {
                return List.of(); // Невалидный вид животного
            }
        }

        PetStatus statusEnum = null;
        if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
            try {
                statusEnum = PetStatus.fromValue(filter.getStatus().toLowerCase());
            } catch (IllegalArgumentException e) {
                return List.of(); // Невалидный статус
            }
        }

        Integer maxAge = filter.getMaxAge();
        String sortBy = filter.getSortBy() != null ? filter.getSortBy().toLowerCase() : "arrival_date";
        String sortDirection = filter.getSortDirection() != null ? filter.getSortDirection().toLowerCase() : "desc";

        List<Pet> pets = petRepository.findFilteredPets(
                speciesEnum,
                statusEnum,
                maxAge
        );

        // Apply sorting
        pets.sort((p1, p2) -> {
            int comparison;
            switch (sortBy) {
                case "arrival_date":
                    comparison = p1.getArrivalDate().compareTo(p2.getArrivalDate());
                    break;
                case "age":
                    comparison = Integer.compare(
                            p1.getAge() != null ? p1.getAge() : 0,
                            p2.getAge() != null ? p2.getAge() : 0
                    );
                    break;
                case "weight_kg":
                    comparison = Double.compare(
                            p1.getWeightKg() != null ? p1.getWeightKg() : 0.0,
                            p2.getWeightKg() != null ? p2.getWeightKg() : 0.0
                    );
                    break;
                case "name":
                    comparison = p1.getName().compareTo(p2.getName());
                    break;
                default:
                    comparison = p1.getArrivalDate().compareTo(p2.getArrivalDate());
            }
            return sortDirection.equals("asc") ? comparison : -comparison;
        });

        return pets.stream()
                .map(PetResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public PetResponse getPetById(Long id) {
        return petRepository.findById(id)
                .map(PetResponse::fromEntity)
                .orElse(null);
    }

    public PetResponse updatePet(Long id, PetUpdateRequest request) {
        return petRepository.findById(id)
                .map(pet -> {
                    if (request.getName() != null) {
                        pet.setName(request.getName());
                    }
                    if (request.getBreed() != null) {
                        pet.setBreed(request.getBreed());
                    }
                    if (request.getAge() != null) {
                        pet.setAge(request.getAge());
                    }
                    if (request.getColor() != null) {
                        pet.setColor(request.getColor());
                    }
                    if (request.getWeightKg() != null) {
                        pet.setWeightKg(request.getWeightKg());
                    }
                    if (request.getHealthStatus() != null) {
                        pet.setHealthStatus(request.getHealthStatus());
                    }
                    if (request.getDescription() != null) {
                        pet.setDescription(request.getDescription());
                    }
                    if (request.getStatus() != null) {
                        pet.setStatus(request.getStatus());
                    }
                    pet = petRepository.save(pet);
                    return PetResponse.fromEntity(pet);
                })
                .orElse(null);
    }

    public boolean deletePet(Long id) {
        if (petRepository.existsById(id)) {
            petRepository.deleteById(id);
            return true;
        }
        return false;
    }
}