package com.example.demo.enumPackage;

import lombok.Getter;

@Getter
public enum Status {
    ACTIVE("active"),
    INACTIVE("inactive");

    private final String value;

    Status(String value) {
        this.value = value;
    }

    public static boolean isValid(String value) {
        for (Status status : Status.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    public static void validate(String value) {
        if (!isValid(value)) {
            throw new IllegalArgumentException("Unknown status value: " + value);
        }
    }
}
