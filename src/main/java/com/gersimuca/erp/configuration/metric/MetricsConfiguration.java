package com.gersimuca.erp.configuration.metric;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

@Configuration
public class MetricsConfiguration {

  @Bean
  MeterRegistryCustomizer<MeterRegistry> metrics() {
    return registry ->
        registry
            .config()
            .meterFilter(
                new MeterFilter() {
                  @Override
                  @NonNull
                  public Meter.Id map(@NonNull Meter.Id id) {
                    return id.withName(String.format("erp_%s", id.getName()));
                  }
                });
  }
}
