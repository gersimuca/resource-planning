package com.gersimuca.erp.feature.lead;

import com.gersimuca.erp.common.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
interface LeadRepository extends BaseRepository<LeadEntity, Long> {

  @Query(
      """
        SELECT l FROM LeadEntity l
        WHERE (:status IS NULL OR l.status = :status)
          AND (:source IS NULL OR l.source = :source)
          AND (:ownerId IS NULL OR l.ownerId = :ownerId)
          AND (:search IS NULL
               OR LOWER(l.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(l.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(l.email) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(l.company) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
  Page<LeadEntity> search(
      @Param("status") LeadStatus status,
      @Param("source") LeadSource source,
      @Param("ownerId") Long ownerId,
      @Param("search") String search,
      Pageable pageable);
}
