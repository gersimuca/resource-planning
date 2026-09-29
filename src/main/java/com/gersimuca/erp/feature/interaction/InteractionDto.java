package com.gersimuca.erp.feature.interaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author gersimuca
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class InteractionDto {
  private Long interactionId;
  private Long customerId;
  private Long leadId;
  @NotNull private Type type;
  @NotBlank private String subject;
  private String description;
  @NotNull private Status status;
  private OffsetDateTime occurredAt;
  private Long ownerId;
}
