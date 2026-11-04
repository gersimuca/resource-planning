package com.gersimuca.erp.configuration.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullableJackson3Module;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import tools.jackson.databind.json.JsonMapper;

class JacksonConfigTest {

  @Test
  void jsonNullableCustomizer_shouldRegisterModule() {
    JacksonConfig config = new JacksonConfig();
    JsonMapper.Builder builder = JsonMapper.builder();
    JsonMapperBuilderCustomizer customizer = config.jsonNullableCustomizer();
    customizer.customize(builder);
    JsonMapper mapper = builder.build();
    assertThat(mapper.registeredModules()).anyMatch(JsonNullableJackson3Module.class::isInstance);
  }
}
