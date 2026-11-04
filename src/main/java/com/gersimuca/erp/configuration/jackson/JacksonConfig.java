package com.gersimuca.erp.configuration.jackson;

import org.openapitools.jackson.nullable.JsonNullableJackson3Module;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class JacksonConfig {

  @Bean
  public JsonMapperBuilderCustomizer jsonNullableCustomizer() {
    return builder -> builder.addModule(new JsonNullableJackson3Module());
  }
}
