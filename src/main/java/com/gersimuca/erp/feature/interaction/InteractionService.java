package com.gersimuca.erp.feature.interaction;

/**
 * @author gersimuca
 */
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InteractionService {

  Page<InteractionDto> search(
      Long customerId, Long leadId, Type type, Status status, Long ownerId, Pageable pageable);

  InteractionDto findById(Long id);

  InteractionDto create(InteractionDto dto);

  InteractionDto update(Long id, InteractionDto dto);

  void delete(Long id);
}
