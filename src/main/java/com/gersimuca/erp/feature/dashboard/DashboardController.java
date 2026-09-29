package com.gersimuca.erp.feature.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

  private final DashboardService service;

  @GetMapping("/summary")
  public DashboardSummaryDto summary() {
    return service.getSummary();
  }
}
