package com.gersimuca.erp.feature.lead;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class LeadDto {
  private Long leadId;
  @NotBlank private String firstName;
  @NotBlank private String lastName;
  @Email private String email;
  private String phone;
  private String company;
  @NotNull private LeadSource source;
  @NotNull private LeadStatus status;
  private BigDecimal estimatedValue;
  private Long ownerId;
  private Long convertedCustomerId;
  private String notes;
}
