package com.gersimuca.erp.feature.interaction;

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
@RequestMapping("/interactions")
@RequiredArgsConstructor
public class InteractionController {

  private final InteractionService service;

  @GetMapping
  public Page<InteractionDto> search(
      @RequestParam(required = false) final Long customerId,
      @RequestParam(required = false) final Long leadId,
      @RequestParam(required = false) final Type type,
      @RequestParam(required = false) final Status status,
      @RequestParam(required = false) final Long ownerId,
      @PageableDefault(size = 20) final Pageable pageable) {
    return service.search(customerId, leadId, type, status, ownerId, pageable);
  }

  @GetMapping("/{id}")
  public InteractionDto findById(@PathVariable final Long id) {
    return service.findById(id);
  }

  @PostMapping
  public ResponseEntity<InteractionDto> create(@Valid @RequestBody final InteractionDto dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
  }

  @PutMapping("/{id}")
  public InteractionDto update(
      @PathVariable final Long id, @Valid @RequestBody final InteractionDto dto) {
    return service.update(id, dto);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable final Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}
