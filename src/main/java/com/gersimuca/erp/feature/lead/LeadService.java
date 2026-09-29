package com.gersimuca.erp.feature.lead;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LeadService {

  Page<LeadDto> search(
      LeadStatus status, LeadSource source, Long ownerId, String search, Pageable pageable);

  LeadDto findById(Long id);

  LeadDto create(LeadDto dto);

  LeadDto update(Long id, LeadDto dto);

  void delete(Long id);

  /** Creates a Customer from this lead's details and marks the lead CONVERTED. */
  LeadDto convertToCustomer(Long id);
}
