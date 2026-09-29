package com.gersimuca.erp.feature.interaction;

import com.gersimuca.erp.common.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * @author gersimuca
 */
@Repository
interface InteractionRepository extends BaseRepository<InteractionEntity, Long> {

  @Query(
      """
        SELECT i FROM InteractionEntity i
        WHERE (:customerId IS NULL OR i.customerId = :customerId)
          AND (:leadId IS NULL OR i.leadId = :leadId)
          AND (:type IS NULL OR i.type = :type)
          AND (:customerStatus IS NULL OR i.customerStatus = :customerStatus)
          AND (:ownerId IS NULL OR i.ownerId = :ownerId)
        ORDER BY i.occurredAt DESC
        """)
  Page<InteractionEntity> search(
      @Param("customerId") Long customerId,
      @Param("leadId") Long leadId,
      @Param("type") Type type,
      @Param("customerStatus") Status customerStatus,
      @Param("ownerId") Long ownerId,
      Pageable pageable);
}
