package com.gersimuca.erp.feature.dashboard;

import com.gersimuca.erp.feature.customer.CustomerService;
import com.gersimuca.erp.feature.customer.CustomerStatus;
import com.gersimuca.erp.feature.deal.DealDto;
import com.gersimuca.erp.feature.deal.DealService;
import com.gersimuca.erp.feature.deal.DealStage;
import com.gersimuca.erp.feature.interaction.InteractionDto;
import com.gersimuca.erp.feature.interaction.InteractionService;
import com.gersimuca.erp.feature.lead.LeadService;
import com.gersimuca.erp.feature.lead.LeadStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Deliberately talks to the other features only through their public Service interfaces, never
 * their repositories - the same boundary the rest of the codebase enforces by making
 * Repository/Mapper package-private. At this data scale the extra round trips are free; a busier
 * deployment would want dedicated count/sum queries behind the same interfaces.
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

  private static final BigDecimal ZERO = BigDecimal.ZERO;

  private final CustomerService customerService;
  private final LeadService leadService;
  private final InteractionService interactionService;
  private final DealService dealService;

  @Override
  @Transactional(readOnly = true)
  public DashboardSummaryDto getSummary() {
    final long totalCustomers =
        customerService.search(null, null, null, 0, 20, null).getMetadataDto().getTotalElements();

    final long activeCustomers =
        customerService
            .search(CustomerStatus.ACTIVE, null, null, 0, 20, null)
            .getMetadataDto()
            .getTotalElements();

    final Map<String, Long> leadsByStatus =
        Arrays.stream(LeadStatus.values())
            .collect(
                Collectors.toMap(
                    Enum::name,
                    status ->
                        leadService
                            .search(status, null, null, null, Pageable.unpaged())
                            .getTotalElements()));
    final long totalLeads = leadsByStatus.values().stream().mapToLong(Long::longValue).sum();

    final List<DealDto> openPipeline = dealService.findOpenPipeline();
    final BigDecimal openPipelineValue = sumAmounts(openPipeline);
    final List<DashboardSummaryDto.StageBreakdown> dealsByStage =
        openPipeline.stream()
            .collect(Collectors.groupingBy(deal -> deal.getStage().name()))
            .entrySet()
            .stream()
            .map(
                entry ->
                    DashboardSummaryDto.StageBreakdown.builder()
                        .stage(entry.getKey())
                        .count(entry.getValue().size())
                        .value(sumAmounts(entry.getValue()))
                        .build())
            .toList();

    final Page<DealDto> closedWon =
        dealService.search(DealStage.CLOSED_WON, null, null, Pageable.unpaged());

    final Page<InteractionDto> allInteractions =
        interactionService.search(null, null, null, null, null, Pageable.unpaged());
    final OffsetDateTime sevenDaysAgo = OffsetDateTime.now().minusDays(7);
    final long interactionsLast7Days =
        allInteractions.getContent().stream()
            .filter(interaction -> interaction.getOccurredAt() != null)
            .filter(interaction -> interaction.getOccurredAt().isAfter(sevenDaysAgo))
            .count();

    return DashboardSummaryDto.builder()
        .totalCustomers(totalCustomers)
        .activeCustomers(activeCustomers)
        .totalLeads(totalLeads)
        .leadsByStatus(leadsByStatus)
        .openDealsCount(openPipeline.size())
        .openPipelineValue(openPipelineValue)
        .closedWonDealsCount(closedWon.getTotalElements())
        .closedWonValue(sumAmounts(closedWon.getContent()))
        .dealsByStage(dealsByStage)
        .totalInteractions(allInteractions.getTotalElements())
        .interactionsLast7Days(interactionsLast7Days)
        .build();
  }

  private BigDecimal sumAmounts(final List<DealDto> deals) {
    return deals.stream()
        .map(DealDto::getAmount)
        .filter(Objects::nonNull)
        .reduce(ZERO, BigDecimal::add);
  }
}
