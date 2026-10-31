package com.candronex.drone.domain;

import java.util.regex.Pattern;

public record DroneId(String value) {

    private static final Pattern FORMAT = Pattern.compile("^[A-Za-z0-9._-]{1,64}$");

    public DroneId {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw DomainException.validation("VALIDATION_ERROR",
                    "droneId invalide (1 à 64 caractères : lettres, chiffres, point, tiret, souligné)");
        }
    }

    public static DroneId of(String value) { return new DroneId(value); }

    @Override
    public String toString() { return value; }
}
