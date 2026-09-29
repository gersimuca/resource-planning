package com.gersimuca.erp.feature.lead;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
class LeadMapper {

  public LeadDto toDto(final LeadEntity entity) {
    if (entity == null) {
      return null;
    }
    return LeadDto.builder()
        .leadId(entity.getLeadId())
        .firstName(entity.getFirstName())
        .lastName(entity.getLastName())
        .email(entity.getEmail())
        .phone(entity.getPhone())
        .company(entity.getCompany())
        .source(entity.getSource())
        .status(entity.getStatus())
        .estimatedValue(entity.getEstimatedValue())
        .ownerId(entity.getOwnerId())
        .convertedCustomerId(entity.getConvertedCustomerId())
        .notes(entity.getNotes())
        .build();
  }

  public List<LeadDto> toDtoList(final List<LeadEntity> entities) {
    return entities.stream().map(this::toDto).toList();
  }

  public LeadEntity toEntity(final LeadDto dto) {
    return LeadEntity.builder()
        .firstName(dto.getFirstName())
        .lastName(dto.getLastName())
        .email(dto.getEmail())
        .phone(dto.getPhone())
        .company(dto.getCompany())
        .source(dto.getSource())
        .status(dto.getStatus())
        .estimatedValue(dto.getEstimatedValue())
        .ownerId(dto.getOwnerId())
        .notes(dto.getNotes())
        .build();
  }

  public void copyToEntity(final LeadDto dto, final LeadEntity entity) {
    entity.setFirstName(dto.getFirstName());
    entity.setLastName(dto.getLastName());
    entity.setEmail(dto.getEmail());
    entity.setPhone(dto.getPhone());
    entity.setCompany(dto.getCompany());
    entity.setSource(dto.getSource());
    entity.setStatus(dto.getStatus());
    entity.setEstimatedValue(dto.getEstimatedValue());
    entity.setOwnerId(dto.getOwnerId());
    entity.setNotes(dto.getNotes());
  }
}
