package com.candronex.catalog.api;

import java.net.URI;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/** Fabrique de Problem Details (RFC 9457) : type, title, status, detail, instance, code, correlationId. */
public final class Problems {

    private Problems() { }

    public static ProblemDetail of(HttpStatus status, String code, String detail, String instance) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setType(URI.create("urn:candronex:problem:" + code));
        pd.setTitle(status.getReasonPhrase());
        if (instance != null) pd.setInstance(URI.create(instance));
        pd.setProperty("code", code);
        pd.setProperty("correlationId", MDC.get("correlationId"));
        return pd;
    }
}
