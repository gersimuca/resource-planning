package com.gersimuca.erp.feature.interaction;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
class InteractionMapper {

  public InteractionDto toDto(final InteractionEntity entity) {
    if (entity == null) {
      return null;
    }
    return InteractionDto.builder()
        .interactionId(entity.getInteractionId())
        .customerId(entity.getCustomerId())
        .leadId(entity.getLeadId())
        .type(entity.getType())
        .subject(entity.getSubject())
        .description(entity.getDescription())
        .status(entity.getStatus())
        .occurredAt(entity.getOccurredAt())
        .ownerId(entity.getOwnerId())
        .build();
  }

  public List<InteractionDto> toDtoList(final List<InteractionEntity> entities) {
    return entities.stream().map(this::toDto).toList();
  }

  public InteractionEntity toEntity(final InteractionDto dto) {
    return InteractionEntity.builder()
        .customerId(dto.getCustomerId())
        .leadId(dto.getLeadId())
        .type(dto.getType())
        .subject(dto.getSubject())
        .description(dto.getDescription())
        .status(dto.getStatus())
        .occurredAt(dto.getOccurredAt())
        .ownerId(dto.getOwnerId())
        .build();
  }

  public void copyToEntity(final InteractionDto dto, final InteractionEntity entity) {
    entity.setCustomerId(dto.getCustomerId());
    entity.setLeadId(dto.getLeadId());
    entity.setType(dto.getType());
    entity.setSubject(dto.getSubject());
    entity.setDescription(dto.getDescription());
    entity.setStatus(dto.getStatus());
    entity.setOccurredAt(dto.getOccurredAt());
    entity.setOwnerId(dto.getOwnerId());
  }
}
