package com.gersimuca.erp.feature.deal;

import com.gersimuca.erp.common.exception.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DealServiceImpl implements DealService {

  private static final List<Stage> CLOSED_STAGES = List.of(Stage.CLOSED_WON, Stage.CLOSED_LOST);

  private final DealRepository dealRepository;
  private final DealMapper dealMapper;

  @Override
  public Page<DealDto> search(
      final Stage stage, final Long ownerId, final Long customerId, final Pageable pageable) {

    return dealRepository.search(stage, ownerId, customerId, pageable).map(dealMapper::toDto);
  }

  @Override
  public List<DealDto> findOpenPipeline() {
    final List<DealEntity> deals =
        dealRepository.findByStageNotInOrderByExpectedCloseDateAsc(CLOSED_STAGES);

    return dealMapper.toDtoList(deals);
  }

  @Override
  public DealDto findById(final Long id) {
    return dealMapper.toDto(findDealOrThrow(id));
  }

  @Override
  @Transactional
  public DealDto create(final DealDto dto) {
    final DealEntity deal = dealMapper.toEntity(dto);
    final DealEntity savedDeal = dealRepository.save(deal);

    return dealMapper.toDto(savedDeal);
  }

  @Override
  @Transactional
  public DealDto update(final Long id, final DealDto dto) {
    final DealEntity deal = findDealOrThrow(id);

    dealMapper.copyToEntity(dto, deal);

    final DealEntity updatedDeal = dealRepository.save(deal);

    return dealMapper.toDto(updatedDeal);
  }

  @Override
  @Transactional
  public void delete(final Long id) {
    dealRepository.delete(findDealOrThrow(id));
  }

  private DealEntity findDealOrThrow(final Long id) {
    return dealRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(DealEntity.class, id));
  }
}
