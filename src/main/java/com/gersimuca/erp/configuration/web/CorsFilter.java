package com.gersimuca.erp.configuration.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component("customCorsFilter")
@Order(Ordered.HIGHEST_PRECEDENCE)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class CorsFilter implements Filter {

  private final List<String> allowedOrigins = new ArrayList<>();

  @Value("${cors.allowed-origins}")
  private String allowedOriginsString;

  @Override
  public void init(FilterConfig filterConfig) {
    var tokenizer = new StringTokenizer(allowedOriginsString, ",");

    while (tokenizer.hasMoreTokens()) {
      allowedOrigins.add(tokenizer.nextToken().trim());
    }
  }

  @Override
  public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain)
      throws IOException, ServletException {
    var response = (HttpServletResponse) res;
    var request = (HttpServletRequest) req;
    var originHeader = request.getHeader("Origin");

    if (allowedOrigins.contains(originHeader)) {
      response.setHeader("Access-Control-Allow-Origin", originHeader);
    }

    response.setHeader("Access-Control-Expose-Headers", "Allow");
    response.setHeader("Access-Control-Allow-Credentials", "true");
    response.setHeader(
        "Access-Control-Allow-Methods", "POST, PUT, PATCH, GET, OPTIONS, DELETE, QUERY");
    response.setHeader("Access-Control-Max-Age", "3600");
    response.setHeader(
        "Access-Control-Allow-Headers", "x-requested-with, authorization, content-type");

    if ("OPTIONS".equalsIgnoreCase(request.getMethod())
        && request.getHeader("Authorization") == null) {
      response.setStatus(HttpServletResponse.SC_OK);
    } else {
      chain.doFilter(req, res);
    }
  }
}
