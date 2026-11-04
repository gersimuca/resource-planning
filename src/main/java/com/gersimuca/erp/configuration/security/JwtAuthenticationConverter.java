package com.gersimuca.erp.configuration.security;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  private static final String U_NUMBER_FIELD = "username";

  @Value("${sso.client-id}")
  private String clientId;

  @Override
  public AbstractAuthenticationToken convert(final Jwt jwt) {
    return new JwtAuthenticationToken(
        jwt, extractResourceRoles(jwt, clientId), Objects.toString(jwt.getClaim(U_NUMBER_FIELD)));
  }

  @SuppressWarnings("unchecked")
  private static Collection<GrantedAuthority> extractResourceRoles(
      final JwtClaimAccessor claimAccessor, final String resourceId) {
    Map<String, Object> resourceAccess = claimAccessor.getClaim("resource_access");
    Map<String, Object> resource;
    Collection<String> resourceRoles;

    if (resourceAccess != null
        && (resource = (Map<String, Object>) resourceAccess.get(resourceId)) != null
        && (resourceRoles = (Collection<String>) resource.get("roles")) != null) {
      return resourceRoles.stream()
          .map(String::toUpperCase)
          .map(SimpleGrantedAuthority::new)
          .collect(toSet());
    }

    return emptySet();
  }
}
