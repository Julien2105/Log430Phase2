package com.candronex.slice.domain;

public class DomainException extends RuntimeException {

    public enum Kind { VALIDATION, FORBIDDEN, NOT_FOUND, CONFLICT, UNPROCESSABLE, UNAVAILABLE, TIMEOUT }

    private final Kind kind;
    private final String code;

    public DomainException(Kind kind, String code, String message) {
        super(message);
        this.kind = kind;
        this.code = code;
    }

    public Kind kind() { return kind; }

    public String code() { return code; }

    public static DomainException validation(String code, String message) { return new DomainException(Kind.VALIDATION, code, message); }

    public static DomainException forbidden(String message) { return new DomainException(Kind.FORBIDDEN, "FORBIDDEN", message); }

    public static DomainException notFound(String message) { return new DomainException(Kind.NOT_FOUND, "NOT_FOUND", message); }

    public static DomainException conflict(String code, String message) { return new DomainException(Kind.CONFLICT, code, message); }

    public static DomainException unprocessable(String code, String message) { return new DomainException(Kind.UNPROCESSABLE, code, message); }

    public static DomainException unavailable(String message) { return new DomainException(Kind.UNAVAILABLE, "SERVICE_UNAVAILABLE", message); }

    public static DomainException timeout(String message) { return new DomainException(Kind.TIMEOUT, "UPSTREAM_TIMEOUT", message); }
}
