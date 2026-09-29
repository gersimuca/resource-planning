package com.gersimuca.erp.feature.interaction;

import com.gersimuca.erp.common.exception.EntityNotFoundException;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {

  private final InteractionRepository repository;
  private final InteractionMapper mapper;

  @Override
  @Transactional(readOnly = true)
  public Page<InteractionDto> search(
      final Long customerId,
      final Long leadId,
      final Type type,
      final Status status,
      final Long ownerId,
      final Pageable pageable) {
    return repository
        .search(customerId, leadId, type, status, ownerId, pageable)
        .map(mapper::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public InteractionDto findById(final Long id) {
    return mapper.toDto(getOrThrow(id));
  }

  @Override
  @Transactional
  public InteractionDto create(final InteractionDto dto) {
    requireRelatedEntity(dto);
    final InteractionEntity entity = mapper.toEntity(dto);
    if (entity.getOccurredAt() == null) {
      entity.setOccurredAt(OffsetDateTime.now());
    }
    return mapper.toDto(repository.save(entity));
  }

  @Override
  @Transactional
  public InteractionDto update(final Long id, final InteractionDto dto) {
    requireRelatedEntity(dto);
    final InteractionEntity entity = getOrThrow(id);
    mapper.copyToEntity(dto, entity);
    if (entity.getOccurredAt() == null) {
      entity.setOccurredAt(OffsetDateTime.now());
    }
    return mapper.toDto(repository.save(entity));
  }

  @Override
  @Transactional
  public void delete(final Long id) {
    repository.delete(getOrThrow(id));
  }

  private void requireRelatedEntity(final InteractionDto dto) {
    if (dto.getCustomerId() == null && dto.getLeadId() == null) {
      throw new IllegalArgumentException(
          "An interaction must reference a customer, a lead, or both.");
    }
  }

  private InteractionEntity getOrThrow(final Long id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(InteractionEntity.class, id));
  }
}
