package com.gersimuca.erp.feature.customer;

import com.gersimuca.erp.common.exception.EntityNotFoundException;
import com.gersimuca.erp.common.pagination.PageableFactory;
import com.gersimuca.erp.feature.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author gersimuca
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

  private final CustomerRepository repository;
  private final CustomerMapper mapper;
  private final PageableFactory pageableFactory;

  @Override
  public CustomersPageDto search(
      final CustomerStatus customerStatus,
      final Long ownerId,
      final String search,
      final Integer page,
      Integer size,
      String sort) {
    final Pageable pageable = pageableFactory.create(page, size, sort);
    final Page<CustomerEntity> customerEntityPage =
        repository.search(customerStatus, ownerId, normalizeSearch(search), pageable);
    return mapper.toPageDto(customerEntityPage);
  }

  @Override
  public CustomerDto findById(final Long id) {
    return mapper.toDto(findEntity(id));
  }

  @Override
  @Transactional
  public CustomerDto create(final CustomerDto dto) {
    final CustomerEntity entity = mapper.toEntity(dto);
    return mapper.toDto(repository.save(entity));
  }

  @Override
  @Transactional
  public CustomerDto update(final Long id, final CustomerDto dto) {
    final CustomerEntity entity = findEntity(id);
    mapper.copyToEntity(dto, entity);
    return mapper.toDto(repository.save(entity));
  }

  @Override
  @Transactional
  public void delete(final Long id) {
    CustomerEntity entity = findEntity(id);
    repository.delete(entity);
  }

  private CustomerEntity findEntity(final Long id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(UserEntity.class, id));
  }

  private String normalizeSearch(final String search) {
    if (search == null || search.isBlank()) {
      return null;
    }
    return search.trim();
  }
}
