package com.example.petshelter.enums;

public enum AnimalSpecies {
    CAT("cat"),
    DOG("dog"),
    BIRD("bird"),
    RABBIT("rabbit"),
    HAMSTER("hamster");

    private final String value;

    AnimalSpecies(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static AnimalSpecies fromValue(String value) {
        for (AnimalSpecies type : AnimalSpecies.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid animal type: " + value);
    }
}
