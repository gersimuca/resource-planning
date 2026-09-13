package com.gersimuca.erp.feature.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gersimuca.erp.AuthenticatedMvcTest;
import com.gersimuca.erp.configuration.jpa.JwtTestData;
import com.gersimuca.erp.configuration.security.JwtAuthenticationConverter;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@AuthenticatedMvcTest
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class UserControllerITest {

  @Autowired private MockMvc mvc;

  @Test
  void createUserEndpointReturnsCreatedUser() throws Exception {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    ReflectionTestUtils.setField(converter, "clientId", "erp");
    mvc.perform(
            post("/users")
                .with(
                    jwt()
                        .jwt(JwtTestData.JWT_NEW_USER)
                        .authorities(
                            jwt ->
                                Objects.requireNonNull(converter.convert(jwt)).getAuthorities())))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
        .andExpect(jsonPath("$.user.username", is("U1234567")))
        .andExpect(jsonPath("$.user.businessKey").isNumber());
  }

  @Test
  void testGetCurrentUserEndpoint() throws Exception {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    ReflectionTestUtils.setField(converter, "clientId", "erp");
    mvc.perform(
            get("/users/current-user")
                .with(
                    jwt()
                        .jwt(JwtTestData.JWT)
                        .authorities()
                        .authorities(
                            jwt ->
                                Objects.requireNonNull(converter.convert(jwt)).getAuthorities())))
        .andExpect(
            authenticated()
                .withAuthentication(
                    auth -> assertThat(auth).isInstanceOf(JwtAuthenticationToken.class)))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
        .andExpect(jsonPath("$.user.username", is("U123456")));
  }

  @Test
  void testGetCurrentUserRolesEndpoint() throws Exception {
    mvc.perform(get("/users/current-user/roles"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
        .andExpect(jsonPath("$.roles", hasSize(3)))
        .andExpect(jsonPath("$.roles", containsInAnyOrder("ADMIN", "USER", "APP_ERP")));
  }
}
