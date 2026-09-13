package com.gersimuca.erp.feature.machine_registry;

import com.gersimuca.erp.common.machine_registry.MachineContext;

/**
 * The scheduled sweep that reclaims machine-id leases abandoned by pods that stopped heartbeating -
 * most often because they crashed, were force-killed, or were partitioned from the database, rather
 * than shut down cleanly through {@link MachineContext#release()}.
 */
public interface MachineRegistryStaleCleanupService {
  void cleanup();
}
