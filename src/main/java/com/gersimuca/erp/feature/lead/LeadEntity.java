package com.gersimuca.erp.feature.lead;

import com.gersimuca.erp.common.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lead")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class LeadEntity extends AuditedEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "lead_id")
  private Long leadId;

  @Column(name = "first_name", nullable = false)
  private String firstName;

  @Column(name = "last_name", nullable = false)
  private String lastName;

  @Column(name = "email")
  private String email;

  @Column(name = "phone")
  private String phone;

  @Column(name = "company")
  private String company;

  @Enumerated(EnumType.STRING)
  @Column(name = "source", nullable = false, length = 30)
  private LeadSource source;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private LeadStatus status;

  @Column(name = "estimated_value", precision = 14, scale = 2)
  private BigDecimal estimatedValue;

  @Column(name = "owner_id")
  private Long ownerId;

  @Column(name = "converted_customer_id")
  private Long convertedCustomerId;

  @Column(name = "notes", columnDefinition = "text")
  private String notes;
}
