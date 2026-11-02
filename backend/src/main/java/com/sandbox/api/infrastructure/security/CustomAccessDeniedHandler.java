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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/** Custom access denied handler for handling authorization errors */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

  private static final Logger logger = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException, ServletException {

    logger.error("Access denied error: {}", accessDeniedException.getMessage());

    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);

    // RFC 7807 Problem Details format
    Map<String, Object> body = new HashMap<>();
    body.put("type", "about:blank");
    body.put("title", "Forbidden");
    body.put("status", HttpServletResponse.SC_FORBIDDEN);
    body.put("detail", "You don't have permission to access this resource");
    body.put("instance", request.getRequestURI());

    JSON_MAPPER.writeValue(response.getOutputStream(), body);
  }
}
