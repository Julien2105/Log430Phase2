package com.candronex.drone.api;

import com.candronex.drone.domain.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** Toutes les erreurs sont retournées en application/problem+json (RFC 9457). */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetail> domain(DomainException e, HttpServletRequest req) {
        HttpStatus status = switch (e.kind()) {
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case UNPROCESSABLE -> HttpStatus.UNPROCESSABLE_ENTITY;
            case UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case TIMEOUT -> HttpStatus.GATEWAY_TIMEOUT;
        };
        if (status.is5xxServerError()) LOG.warn("event=DOWNSTREAM_ERROR code={}", e.code());
        return build(Problems.of(status, e.code(), e.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> invalid(MethodArgumentNotValidException e, HttpServletRequest req) {
        List<Map<String, String>> errors = e.getBindingResult().getFieldErrors().stream()
                .map(f -> Map.of("field", f.getField(), "message", String.valueOf(f.getDefaultMessage())))
                .toList();
        ProblemDetail pd = Problems.of(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Requête invalide", req.getRequestURI());
        pd.setProperty("errors", errors);
        return build(pd);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingRequestHeaderException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ProblemDetail> badRequest(Exception e, HttpServletRequest req) {
        return build(Problems.of(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Requête mal formée ou paramètre manquant ou invalide", req.getRequestURI()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> noResource(NoResourceFoundException e, HttpServletRequest req) {
        return build(Problems.of(HttpStatus.NOT_FOUND, "NOT_FOUND", "Ressource introuvable", req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> unexpected(Exception e, HttpServletRequest req) {
        LOG.error("event=UNEXPECTED_ERROR", e);
        return build(Problems.of(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Erreur interne inattendue", req.getRequestURI()));
    }

    private static ResponseEntity<ProblemDetail> build(ProblemDetail pd) {
        return ResponseEntity.status(pd.getStatus()).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(pd);
    }
}
