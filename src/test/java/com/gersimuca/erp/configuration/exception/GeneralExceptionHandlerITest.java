package com.gersimuca.erp.configuration.exception;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gersimuca.erp.AuthenticatedMvcTest;
import com.gersimuca.erp.common.exception.BaseException;
import com.gersimuca.erp.common.exception.EntityNotFoundException;
import com.gersimuca.erp.common.exception.ErrorSeverity;
import com.gersimuca.erp.common.exception.ProblemTypes;
import com.gersimuca.erp.configuration.jpa.JwtTestData;
import com.gersimuca.erp.configuration.security.JwtAuthenticationConverter;
import com.gersimuca.erp.feature.user.UserEntity;
import com.gersimuca.erp.model.ErrorCode;
import java.util.Objects;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.openapitools.jackson.nullable.JsonNullableJackson3Module;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

@AuthenticatedMvcTest
@SpringBootTest
class GeneralExceptionHandlerITest {

  @Autowired private MockMvc mvc;

  @Test
  void handleBaseException() throws Exception {

    HttpStatus notFound = HttpStatus.NOT_FOUND;

    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    ReflectionTestUtils.setField(converter, "clientId", "erp");

    EntityNotFoundException ex = new EntityNotFoundException(UserEntity.class, "U159785");

    final String path = "/users/current-user";
    mvc.perform(
            get(path)
                .contentType(MediaType.APPLICATION_JSON)
                .with(
                    jwt()
                        .jwt(JwtTestData.JWT_NOT_EXISTING_USERNAME)
                        .authorities()
                        .authorities(
                            jwt ->
                                Objects.requireNonNull(converter.convert(jwt)).getAuthorities())))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status", is(notFound.value())))
        .andExpect(jsonPath("$.title", is(notFound.getReasonPhrase())))
        .andExpect(jsonPath("$.code", is(ex.getErrorCode().getValue())))
        .andExpect(jsonPath("$.detail", is(ex.getMessage())))
        .andExpect(jsonPath("$.type", is(ProblemTypes.of(ex.getErrorCode()).toString())))
        .andExpect(jsonPath("$.instance", is(path)))
        .andExpect(jsonPath("$.traceId").exists())
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.errors").exists())
        .andExpect(jsonPath("$.errors").isArray());
  }

  @WithMockUser(username = "iron.man")
  @Test
  void handleAccessDeniedException() throws Exception {
    final HttpStatus forbidden = HttpStatus.FORBIDDEN;
    final BaseException ex =
        new BaseException("Access Denied", forbidden, ErrorSeverity.WARN, ErrorCode.ACCESS_DENIED);
    final String path = "/users/current-user";
    mvc.perform(get(path))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status", is(forbidden.value())))
        .andExpect(jsonPath("$.title", is(forbidden.getReasonPhrase())))
        .andExpect(jsonPath("$.code", is(ex.getErrorCode().getValue())))
        .andExpect(jsonPath("$.detail", is(ex.getMessage())))
        .andExpect(jsonPath("$.type", is(ProblemTypes.of(ex.getErrorCode()).toString())))
        .andExpect(jsonPath("$.instance", is(path)))
        .andExpect(jsonPath("$.traceId").exists())
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.errors").exists())
        .andExpect(jsonPath("$.errors").isArray());
  }

  @Test
  void handleConstraintViolationException() throws Exception {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    ReflectionTestUtils.setField(converter, "clientId", "erp");
    String path = "/users";
    mvc.perform(
            post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .with(
                    jwt()
                        .jwt(JwtTestData.JWT_INVALID_USERNAME)
                        .authorities()
                        .authorities(
                            jwt -> Objects.requireNonNull(converter.convert(jwt)).getAuthorities()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
        .andExpect(jsonPath("$.title", is(HttpStatus.BAD_REQUEST.getReasonPhrase())))
        .andExpect(jsonPath("$.code", is(ErrorCode.INVALID_REQUEST.name())))
        .andExpect(jsonPath("$.type", is(ProblemTypes.of(ErrorCode.INVALID_REQUEST).toString())))
        .andExpect(jsonPath("$.instance", is(path)))
        .andExpect(jsonPath("$.traceId").exists())
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.errors").exists())
        .andExpect(jsonPath("$.errors").isArray())
        .andExpect(jsonPath("$.errors[0].field", is("username")))
        .andExpect(jsonPath("$.errors[0].issue", is("Username is mandatory")))
        .andExpect(jsonPath("$.errors[0].rejectedValue", is("")));
  }

  /**
   * @author gersimuca
   */
  @Nested
  class JsonNullableDeserializationTest {
    static class Sample {
      public JsonNullable<String> field;
    }

    @Test
    void shouldDeserializePresentValue() throws Exception {
      JsonMapper mapper = JsonMapper.builder().addModule(new JsonNullableJackson3Module()).build();

      Sample result = mapper.readValue("{\"field\":\"hello\"}", Sample.class);

      assertThat(result.field.isPresent()).isTrue();
      assertThat(result.field.get()).isEqualTo("hello");
    }

    @Test
    void shouldDeserializeNullAsPresentNull() throws Exception {
      JsonMapper mapper = JsonMapper.builder().addModule(new JsonNullableJackson3Module()).build();

      Sample result = mapper.readValue("{\"field\":null}", Sample.class);

      assertThat(result.field.isPresent()).isTrue();
      assertThat(result.field.get()).isNull();
    }
  }
}
