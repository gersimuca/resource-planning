package com.gersimuca.erp.feature.interaction;

import com.gersimuca.erp.common.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single logged touchpoint - a call, email, meeting, note or support request - tied to a customer
 * and/or lead.
 */
@Entity
@Table(name = "interaction")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class InteractionEntity extends AuditedEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "interaction_id")
  private Long interactionId;

  @Column(name = "customer_id")
  private Long customerId;

  @Column(name = "lead_id")
  private Long leadId;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 20)
  private Type type;

  @Column(name = "subject", nullable = false)
  private String subject;

  @Column(name = "description", columnDefinition = "text")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private Status status;

  @Column(name = "occurred_at", nullable = false)
  private OffsetDateTime occurredAt;

  @Column(name = "owner_id")
  private Long ownerId;
}
