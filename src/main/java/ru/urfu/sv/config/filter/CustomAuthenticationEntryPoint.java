package ru.urfu.sv.config.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    @SneakyThrows
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");

        if (request.getHeader(HttpHeaders.AUTHORIZATION) == null)
            setResponseError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed: Invalid Authorization Header");
        else
            setResponseError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed: Invalid Json Web Token");
    }

    private void setResponseError(HttpServletResponse response, int errorCode, String errorMessage) throws IOException {
        try (PrintWriter writer = response.getWriter()) {
            response.setStatus(errorCode);
            response.setCharacterEncoding("UTF-8");
            writer.write(errorMessage);
        }
    }
}