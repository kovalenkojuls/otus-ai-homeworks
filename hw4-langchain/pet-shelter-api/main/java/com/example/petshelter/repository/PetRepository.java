package com.example.petshelter.repository;

import com.example.petshelter.entity.Pet;
import com.example.petshelter.enums.AnimalSpecies;
import com.example.petshelter.enums.PetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    @Query("SELECT p FROM Pet p WHERE " +
            "(:species IS NULL OR p.species = :species) AND " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:maxAge IS NULL OR p.age <= :maxAge)")
    List<Pet> findFilteredPets(@Param("species") AnimalSpecies species,
                               @Param("status") PetStatus status,
                               @Param("maxAge") Integer maxAge);
}