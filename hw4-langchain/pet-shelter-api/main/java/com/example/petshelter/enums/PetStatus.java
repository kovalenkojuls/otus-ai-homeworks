package com.example.petshelter.enums;

public enum PetStatus {
    AVAILABLE("available"),
    ADOPTED("adopted"),
    FOSTERED("fostered"),
    TREATMENT("treatment");

    private final String value;

    PetStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static PetStatus fromValue(String value) {
        for (PetStatus status : PetStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid pet status: " + value);
    }
}