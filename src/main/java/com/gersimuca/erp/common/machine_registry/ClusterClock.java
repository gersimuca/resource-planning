package com.gersimuca.erp.common.machine_registry;

import java.time.Instant;
import org.springframework.stereotype.Component;

/**
 * Wraps the system wall clock as a {@link BusinessTimestamp}, offset by a fixed epoch so the value
 * fits comfortably inside the bits {@link BusinessKeyLayout} allocates for it.
 *
 * <p>This is a thin wrapper around {@link Instant#now()} - it does not itself guarantee
 * monotonicity across calls (the OS clock can still step backwards under NTP correction).
 * Monotonicity is enforced one layer up, in {@link BusinessKeyGenerator#nextId()}, which never lets
 * its logical timestamp move backwards even if this method returns a smaller value than it did on
 * the previous call.
 */
@Component
public class ClusterClock {

  /**
   * Epoch this clock measures from: 2025-01-01T00:00:00Z, in epoch milliseconds. Purely to keep the
   * encoded timestamp small - fixed forever once any id has been generated against it, for the same
   * reason changing {@link BusinessKeyLayout} is a migration, not a config change.
   */
  private static final long EPOCH_MILLIS = 1735689600000L; // 2025-01-01T00:00:00Z

  /** The current time, expressed as milliseconds since {@link #EPOCH_MILLIS}. */
  public BusinessTimestamp now() {
    return new BusinessTimestamp(Instant.now().toEpochMilli() - EPOCH_MILLIS);
  }
}
