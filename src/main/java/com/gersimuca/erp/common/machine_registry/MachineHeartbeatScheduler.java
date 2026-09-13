package com.gersimuca.erp.common.machine_registry;

import com.gersimuca.erp.common.util.LoggerUtils;
import com.gersimuca.erp.feature.machine_registry.MachineRegistryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Keeps this pod's machine-id lease alive by periodically proving it's still up, and triggers a
 * fresh allocation the moment it discovers that proof failed.
 *
 * <p>This is the piece that was missing from the original design: a lease that's never refreshed
 * looks identical, from {@link MachineRegistryStaleCleanupServiceImpl}'s point of view, to a lease
 * held by a pod that crashed - so without this scheduler, every pod's machine id would be reclaimed
 * and handed to someone else within the stale-cleanup cutoff, whether or not the original pod was
 * still alive and still using it.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MachineHeartbeatScheduler {

  private final MachineRegistryService registryService;
  private final MachineContext machineContext;

  /**
   * Fires every 10s - comfortably inside {@link MachineRegistryStaleCleanupServiceImpl}'s 120s
   * cutoff, so a single missed tick (a slow GC pause, a momentary DB hiccup) doesn't by itself risk
   * the lease being reclaimed; several consecutive misses would have to happen first.
   */
  @Scheduled(fixedRate = 10_000)
  public void heartbeat() {
    final Long currentMachineId = machineContext.getMachineId();
    if (currentMachineId == null) {
      // Bean not fully initialized yet (MachineContext.init() hasn't completed) - nothing to
      // heartbeat for yet, and no error either; the next tick picks it up once startup finishes.
      return;
    }

    final String instanceId = machineContext.getInstanceId();
    final boolean stillOwned =
        registryService.updateHeartbeat(new MachineId(currentMachineId), instanceId);
    if (!stillOwned) {
      // The registry no longer has a row matching (currentMachineId, instanceId) - this pod's
      // lease was reclaimed as stale and very possibly already reassigned to a different pod.
      // Continuing to mint ids under the old value risks colliding with whoever holds it now,
      // so get a new one immediately.
      LoggerUtils.error(
          log,
          "machineId={} instanceId={} lost ownership before this heartbeat arrived - reallocating",
          currentMachineId,
          instanceId);
      machineContext.reallocate();
    }
  }
}
