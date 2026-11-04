package com.gersimuca.erp.configuration.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.cache.actuate.endpoint.CachesEndpoint;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.micrometer.metrics.actuate.endpoint.MetricsEndpoint;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity(debug = true)
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationConverter jwtAuthenticationConverter;

  private static final String[] ALLOW_LIST = {
    "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/resources/**"
  };

  @Bean
  protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    var tokenResolver = new DefaultBearerTokenResolver();
    tokenResolver.setAllowUriQueryParameter(true);

    http.authorizeHttpRequests(
            request ->
                request
                    .requestMatchers(ALLOW_LIST)
                    .permitAll()
                    .requestMatchers(getPublicEndpoints())
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            oauthConfigurer ->
                oauthConfigurer
                    .bearerTokenResolver(tokenResolver)
                    .jwt(
                        jwtConfigurer ->
                            jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter)));

    return http.build();
  }

  private EndpointRequest.EndpointRequestMatcher getPublicEndpoints() {
    return EndpointRequest.to(
        HealthEndpoint.class,
        PrometheusScrapeEndpoint.class,
        MetricsEndpoint.class,
        InfoEndpoint.class,
        CachesEndpoint.class);
  }
}
