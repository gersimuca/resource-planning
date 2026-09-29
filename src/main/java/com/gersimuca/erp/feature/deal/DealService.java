package com.gersimuca.erp.feature.deal;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DealService {

  Page<DealDto> search(Stage stage, Long ownerId, Long customerId, Pageable pageable);

  /** All deals that have not yet closed, ordered for a kanban-style pipeline board. */
  List<DealDto> findOpenPipeline();

  DealDto findById(Long id);

  DealDto create(DealDto dto);

  DealDto update(Long id, DealDto dto);

  void delete(Long id);
}
