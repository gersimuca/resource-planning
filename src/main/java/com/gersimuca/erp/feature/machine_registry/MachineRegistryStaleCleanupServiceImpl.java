package com.gersimuca.erp.common.machine_registry;

import com.gersimuca.erp.common.util.LoggerUtils;
import java.time.Instant;

import com.gersimuca.erp.feature.machine_registry.MachineRegistryService;
import com.gersimuca.erp.feature.machine_registry.MachineRegistryStaleCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Runs on every pod, independently, once a minute. At small pod counts the redundancy is
 * harmless; at 60+ pods it means 60+ processes each racing to {@code DELETE} the same handful
 * of stale rows every minute - wasted but not incorrect, since the delete is naturally
 * idempotent (whichever pod's delete runs first removes the row; the rest simply match zero
 * rows). If that redundancy becomes worth avoiding, consider a single elected runner or an
 * OpenShift {@code CronJob} instead of every pod scheduling its own copy.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MachineRegistryStaleCleanupServiceImpl implements MachineRegistryStaleCleanupService {

    /**
     * How long a lease can go without a heartbeat before it's considered abandoned. Set well
     * above {@link MachineHeartbeatScheduler}'s 10s interval so a handful of missed ticks - not
     * just one - is what triggers a reclaim, not routine jitter.
     */
    private static final long STALE_AFTER_SECONDS = 120;

    private final MachineRegistryService service;

    @Override
    @Scheduled(fixedRate = 60_000)
    public void cleanup() {
        final Instant cutoff = Instant.now().minusSeconds(STALE_AFTER_SECONDS);
        LoggerUtils.info(log, "Cleaning stale machine registry entries older than {}", cutoff);
        service.cleanupInactiveMachines(cutoff);
    }
}
