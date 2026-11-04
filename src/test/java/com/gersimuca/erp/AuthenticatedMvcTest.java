package com.gersimuca.erp;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.gersimuca.erp.configuration.authenticated.WithMockJwtToken;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

@Target(TYPE)
@Retention(RUNTIME)
@WithMockJwtToken(
    username = "U123456",
    authorities = {"ADMIN", "USER", "APP_ERP"})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public @interface AuthenticatedMvcTest {}
