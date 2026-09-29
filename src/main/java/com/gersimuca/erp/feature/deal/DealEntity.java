package com.gersimuca.erp.feature.deal;

import com.gersimuca.erp.common.AuditedEntity;
import com.gersimuca.erp.feature.customer.CustomerEntity;
import com.gersimuca.erp.feature.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "deal")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class DealEntity extends AuditedEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "deal_id")
  private Long dealId;

  @ManyToOne
  @JoinColumn(name = "customer_id", nullable = false)
  private CustomerEntity customerId;

  @Column(name = "title", nullable = false)
  private String title;

  @Enumerated(EnumType.STRING)
  @Column(name = "stage", nullable = false, length = 20)
  private Stage stage;

  @Column(name = "amount", precision = 14, scale = 2)
  private BigDecimal amount;

  @Column(name = "expected_close_date")
  private LocalDate expectedCloseDate;

  @ManyToOne
  @JoinColumn(name = "owner_id", nullable = false)
  private UserEntity ownerId;

  @Column(name = "notes", columnDefinition = "text")
  private String notes;
}
