package com.sandbox.api.infrastructure.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/** Custom authentication entry point for handling authentication errors */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

  private static final Logger logger =
      LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException, ServletException {

    logger.error("Unauthorized error: {}", authException.getMessage());

    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    // RFC 7807 Problem Details format
    Map<String, Object> body = new HashMap<>();
    body.put("type", "about:blank");
    body.put("title", "Unauthorized");
    body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
    body.put("detail", "Authentication is required to access this resource");
    body.put("instance", request.getRequestURI());

    JSON_MAPPER.writeValue(response.getOutputStream(), body);
  }
}
