package com.gersimuca.erp.feature.lead;

import com.gersimuca.erp.common.exception.EntityNotFoundException;
import com.gersimuca.erp.feature.customer.CustomerDto;
import com.gersimuca.erp.feature.customer.CustomerService;
import com.gersimuca.erp.feature.customer.CustomerStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LeadServiceImpl implements LeadService {

  private final LeadRepository repository;
  private final LeadMapper mapper;
  private final CustomerService customerService;

  @Override
  @Transactional(readOnly = true)
  public Page<LeadDto> search(
      final LeadStatus status,
      final LeadSource source,
      final Long ownerId,
      final String search,
      final Pageable pageable) {
    final String normalizedSearch = (search == null || search.isBlank()) ? null : search.trim();
    return repository
        .search(status, source, ownerId, normalizedSearch, pageable)
        .map(mapper::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public LeadDto findById(final Long id) {
    return mapper.toDto(getOrThrow(id));
  }

  @Override
  @Transactional
  public LeadDto create(final LeadDto dto) {
    return mapper.toDto(repository.save(mapper.toEntity(dto)));
  }

  @Override
  @Transactional
  public LeadDto update(final Long id, final LeadDto dto) {
    final LeadEntity entity = getOrThrow(id);
    mapper.copyToEntity(dto, entity);
    return mapper.toDto(repository.save(entity));
  }

  @Override
  @Transactional
  public void delete(final Long id) {
    repository.delete(getOrThrow(id));
  }

  @Override
  @Transactional
  public LeadDto convertToCustomer(final Long id) {
    final LeadEntity lead = getOrThrow(id);

    if (lead.getStatus() == LeadStatus.CONVERTED && lead.getConvertedCustomerId() != null) {
      return mapper.toDto(lead);
    }

    final String fullName = (lead.getFirstName() + " " + lead.getLastName()).trim();
    final CustomerDto newCustomer =
        customerService.create(
            CustomerDto.builder()
                .name(fullName)
                .email(lead.getEmail())
                .phone(lead.getPhone())
                .company(lead.getCompany())
                .status(CustomerStatus.ACTIVE)
                .ownerId(lead.getOwnerId())
                .notes("Converted from lead #%d.".formatted(lead.getLeadId()))
                .build());

    lead.setStatus(LeadStatus.CONVERTED);
    lead.setConvertedCustomerId(newCustomer.getCustomerId());

    return mapper.toDto(repository.save(lead));
  }

  private LeadEntity getOrThrow(final Long id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(LeadEntity.class, id));
  }
}
