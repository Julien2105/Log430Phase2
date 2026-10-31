package com.candronex.catalog.infrastructure.security;

import com.candronex.catalog.api.Problems;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/** 401 / 403 au format Problem Details, sans détail technique sur la cause du rejet. */
@Component
public class ProblemSecurityHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ProblemSecurityHandler.class);

    private final ObjectMapper mapper;

    public ProblemSecurityHandler(ObjectMapper mapper) { this.mapper = mapper; }

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException e) throws IOException {
        LOG.warn("event=JWT_REJECTED reason={}", e.getClass().getSimpleName());
        res.setHeader("WWW-Authenticate", "Bearer");
        write(res, Problems.of(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED",
                "Jeton JWT absent, expiré ou invalide", req.getRequestURI()));
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException e) throws IOException {
        write(res, Problems.of(HttpStatus.FORBIDDEN, "FORBIDDEN", "Accès refusé", req.getRequestURI()));
    }

    private void write(HttpServletResponse res, org.springframework.http.ProblemDetail pd) throws IOException {
        res.setStatus(pd.getStatus());
        res.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        mapper.writeValue(res.getWriter(), pd);
    }
}
