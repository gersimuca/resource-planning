package com.gersimuca.erp.feature.user;

import static com.gersimuca.erp.feature.user.UserTestData.IRON_MAN_DTO;
import static com.gersimuca.erp.feature.user.UserTestData.IRON_MAN_ENTITY;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gersimuca.erp.common.exception.EntityAlreadyExistsException;
import com.gersimuca.erp.common.exception.EntityNotFoundException;
import com.gersimuca.erp.common.machine_registry.BusinessKeyGenerator;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class UserServiceImplTest {

  @Mock private UserRepository repository;
  @Mock private UserMapper mapper;
  @Mock private Validator validator;
  @Mock private BusinessKeyGenerator businessKeyGenerator;

  @InjectMocks private UserServiceImpl service;

  @BeforeEach
  void contextLoads() {
    assertNotNull(repository);
    assertNotNull(mapper);
    assertNotNull(validator);
    assertNotNull(service);
  }

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    service = new UserServiceImpl(mapper, repository, validator, businessKeyGenerator);

    when(repository.findAll()).thenReturn(List.of(IRON_MAN_ENTITY));
    when(repository.findById(IRON_MAN_ENTITY.getUserId())).thenReturn(Optional.of(IRON_MAN_ENTITY));
    when(repository.findById(argThat(userId -> !userId.equals(IRON_MAN_ENTITY.getUserId()))))
        .thenReturn(Optional.empty());
  }

  @Test
  void mustLoadByUsernameExisting() {
    final String existingUsername = IRON_MAN_ENTITY.getUsername();
    when(repository.findByUsername(existingUsername)).thenReturn(Optional.of(IRON_MAN_ENTITY));
    service.mustLoadByUsername(existingUsername);
    verify(repository).findByUsername(existingUsername);
  }

  @Test
  void mustLoadByUsernameNonExisting() {
    final String nonExistingUsername = "nonExisting";
    when(repository.findByUsername(nonExistingUsername)).thenReturn(Optional.empty());

    assertThrows(
        EntityNotFoundException.class, () -> service.mustLoadByUsername(nonExistingUsername));
  }

  @Test
  void existsByUsername() {
    final String existingUsername = IRON_MAN_ENTITY.getUsername();
    when(repository.existsByUsername(existingUsername)).thenReturn(true);
    assertTrue(service.existsByUsername(existingUsername));

    final String nonExistingUsername = "nonExisting";
    when(repository.existsByUsername(nonExistingUsername)).thenReturn(false);
    assertFalse(service.existsByUsername(nonExistingUsername));
  }

  @Test
  void createUser() {
    final UserDto userDto = IRON_MAN_DTO;
    service.createUser(userDto);
    verify(repository)
        .statefulSave(
            argThat(
                user ->
                    user.getUsername().equals(userDto.getUsername())
                        && user.getGivenName().equals(userDto.getGivenName())
                        && user.getFamilyName().equals(userDto.getFamilyName())
                        && user.getEmail().equals(userDto.getEmail())));
  }

  @Test
  void createUserWhenUserAlreadyExists() {
    final String existingUsername = IRON_MAN_DTO.getUsername();
    when(repository.existsByUsername(existingUsername)).thenReturn(true);

    assertThrows(EntityAlreadyExistsException.class, () -> service.createUser(IRON_MAN_DTO));
    verify(repository, never()).save(ArgumentMatchers.any(UserEntity.class));
  }
}
