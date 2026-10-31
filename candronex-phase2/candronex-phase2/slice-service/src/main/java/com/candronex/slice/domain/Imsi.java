package com.candronex.slice.domain;

import java.util.regex.Pattern;

public record Imsi(String value) {

    private static final Pattern FORMAT = Pattern.compile("^[0-9]{15}$");

    public Imsi {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw DomainException.validation("INVALID_IMSI", "IMSI invalide : 15 chiffres attendus");
        }
    }

    @Override
    public String toString() { return "Imsi[***]"; }
}
