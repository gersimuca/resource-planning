package com.gersimuca.erp.feature.deal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
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
public class DealDto {

  private Long dealId;

  @NotNull private Long customerId;

  @NotBlank private String title;

  @NotNull private Stage stage;

  @PositiveOrZero private BigDecimal amount;

  private LocalDate expectedCloseDate;
  private Long ownerId;
  private String notes;
}
