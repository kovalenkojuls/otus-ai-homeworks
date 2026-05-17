package com.example.petshelter.enums;

public enum HealthStatus {
    HEALTHY("healthy"),
    SICK("sick"),
    INJURED("injured"),
    RECOVERING("recovering");

    private final String value;

    HealthStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static HealthStatus fromValue(String value) {
        for (HealthStatus status : HealthStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid health status: " + value);
    }
}