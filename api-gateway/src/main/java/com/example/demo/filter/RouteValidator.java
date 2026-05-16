package com.example.demo.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    public static final List<String> openApiEndpoints = List.of(
            "/api/v1/auth/register/candidate",
            "/api/v1/auth/register/recruiter",
            "/api/v1/auth/login",
            "/api/v1/auth/google",
            "/api/v1/auth/validate",
            "/api/v1/auth/refresh",
            "/v3/api-docs",
            "/swagger-ui.html",
            "/swagger-ui",
            "/eureka"
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
}
