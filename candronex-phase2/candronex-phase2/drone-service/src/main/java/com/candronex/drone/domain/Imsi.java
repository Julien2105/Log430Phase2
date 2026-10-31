package com.candronex.drone.domain;

import java.util.regex.Pattern;

/** Identité réseau de la SIM : donnée interne, jamais retournée au client ni écrite dans les logs. */
public record Imsi(String value) {

    private static final Pattern FORMAT = Pattern.compile("^[0-9]{15}$");

    public Imsi {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw DomainException.validation("INVALID_IMSI", "IMSI invalide : 15 chiffres attendus");
        }
    }

    public static Imsi of(String value) { return new Imsi(value); }

    @Override
    public String toString() { return "Imsi[***]"; }
}
