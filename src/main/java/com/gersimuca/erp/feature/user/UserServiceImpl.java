package com.gersimuca.erp.feature.user;

import com.gersimuca.erp.common.exception.EntityAlreadyExistsException;
import com.gersimuca.erp.common.exception.EntityNotFoundException;
import com.gersimuca.erp.common.machine_registry.BusinessKeyGenerator;
import com.gersimuca.erp.common.util.LoggerUtils;
import com.gersimuca.erp.common.util.ValidationUtil;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
  private final UserMapper mapper;
  private final UserRepository repository;
  private final Validator validator;

  private final BusinessKeyGenerator keyGenerator;

  @Override
  public UserDto mustLoadByUsername(final String username) {
    final UserEntity userEntity =
        repository
            .findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException(UserEntity.class, username));
    return mapper.mapToDto(userEntity);
  }

  @Override
  @Transactional
  public UserDto createUser(final UserDto userDto) {
    ValidationUtil.validate(validator, userDto);

    final String username = userDto.getUsername();
    if (existsByUsername(username)) {
      throw new EntityAlreadyExistsException(UserEntity.class, username);
    }

    final UserEntity userEntity =
        UserEntity.builder()
            .businessKey(keyGenerator.nextId())
            .username(username)
            .givenName(userDto.getGivenName())
            .familyName(userDto.getFamilyName())
            .email(userDto.getEmail())
            .isActive(true)
            .build();
    LoggerUtils.info(log, "Creating user with businessKey {}", userEntity.getBusinessKey());
    final UserEntity createdUser = repository.statefulSave(userEntity);

    return mapper.mapToDto(createdUser);
  }

  public boolean existsByUsername(final String username) {
    return repository.existsByUsername(username);
  }
}
