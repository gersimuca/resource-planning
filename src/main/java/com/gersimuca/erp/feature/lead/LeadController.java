package com.gersimuca.erp.feature.lead;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/leads")
@RequiredArgsConstructor
public class LeadController {

  private final LeadService service;

  @GetMapping
  public Page<LeadDto> search(
      @RequestParam(required = false) final LeadStatus status,
      @RequestParam(required = false) final LeadSource source,
      @RequestParam(required = false) final Long ownerId,
      @RequestParam(required = false) final String search,
      @PageableDefault(size = 20, sort = "leadId") final Pageable pageable) {
    return service.search(status, source, ownerId, search, pageable);
  }

  @GetMapping("/{id}")
  public LeadDto findById(@PathVariable final Long id) {
    return service.findById(id);
  }

  @PostMapping
  public ResponseEntity<LeadDto> create(@Valid @RequestBody final LeadDto dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
  }

  @PutMapping("/{id}")
  public LeadDto update(@PathVariable final Long id, @Valid @RequestBody final LeadDto dto) {
    return service.update(id, dto);
  }

  @PostMapping("/{id}/convert")
  public LeadDto convert(@PathVariable final Long id) {
    return service.convertToCustomer(id);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable final Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}
