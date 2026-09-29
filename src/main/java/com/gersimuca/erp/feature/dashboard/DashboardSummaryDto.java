package com.gersimuca.erp.feature.dashboard;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

/**
 * Aggregate counters that back the landing dashboard - one call, everything the home screen needs.
 */
@Getter
@Builder
public class DashboardSummaryDto {

  private long totalCustomers;
  private long activeCustomers;

  private long totalLeads;
  private Map<String, Long> leadsByStatus;

  private long openDealsCount;
  private BigDecimal openPipelineValue;
  private long closedWonDealsCount;
  private BigDecimal closedWonValue;
  private List<StageBreakdown> dealsByStage;

  private long totalInteractions;
  private long interactionsLast7Days;

  @Getter
  @Builder
  public static class StageBreakdown {
    private String stage;
    private long count;
    private BigDecimal value;
  }
}
