package com.gersimuca.erp.feature.deal;

import com.gersimuca.erp.common.repository.BaseRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
interface DealRepository extends BaseRepository<DealEntity, Long> {

  @Query(
      """
        SELECT d FROM DealEntity d
        WHERE (:stage IS NULL OR d.stage = :stage)
          AND (:ownerId IS NULL OR d.ownerId = :ownerId)
          AND (:customerId IS NULL OR d.customerId = :customerId)
        """)
  Page<DealEntity> search(
      @Param("stage") Stage stage,
      @Param("ownerId") Long ownerId,
      @Param("customerId") Long customerId,
      Pageable pageable);

  /**
   * Used by the dashboard/pipeline board, which renders every open deal grouped by stage in one
   * shot.
   */
  List<DealEntity> findByStageNotInOrderByExpectedCloseDateAsc(List<Stage> closedStages);
}
