package com.gersimuca.erp.feature.customer;

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
interface CustomerRepository extends BaseRepository<CustomerEntity, Long> {

  @Query(
      """
            SELECT c FROM CustomerEntity c
            WHERE (:customerStatus IS NULL OR c.status = :status)
              AND (:ownerId IS NULL OR c.ownerId = :ownerId)
              AND (:search IS NULL
                   OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(c.company) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
  Page<CustomerEntity> search(
      @Param("status") CustomerStatus status,
      @Param("ownerId") Long ownerId,
      @Param("search") String search,
      Pageable pageable);
}
