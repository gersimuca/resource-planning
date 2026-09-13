/**
 * Cluster-wide, Snowflake-style distributed id generation for OpenShift deployments that may
 * run 60+ pods of the same service concurrently.
 *
 * <h2>How an id is built</h2>
 * Every id is one 64-bit {@code long}, assembled by {@link com.gersimuca.erp.common.machine_registry.BusinessKeyGenerator} from three
 * fields packed together with bit shifts (the exact split lives in
 * {@link com.gersimuca.erp.common.machine_registry.BusinessKeyLayout}):
 * <pre>
 *   high bits                                                              low bits
 *   [ timestamp ][ machine id ][ sequence ]
 * </pre>
 * <ul>
 *   <li><b>timestamp</b> - milliseconds since {@link com.gersimuca.erp.common.machine_registry.ClusterClock}'s epoch, monotonic per
 *       pod, never allowed to move backwards (see {@link com.gersimuca.erp.common.machine_registry.BusinessTimestamp}).</li>
 *   <li><b>machine id</b> - a small integer leased to this pod for as long as it stays alive
 *       (see {@link com.gersimuca.erp.common.machine_registry.MachineId}, {@link com.gersimuca.erp.common.machine_registry.MachineContext}, {@link com.gersimuca.erp.feature.machine_registry.MachineRegistryService}).</li>
 *   <li><b>sequence</b> - disambiguates multiple ids minted by the same pod within the same
 *       logical millisecond (see {@link com.gersimuca.erp.common.machine_registry.BusinessSequence}).</li>
 * </ul>
 *
 * <h2>Why there's a whole registry just for a machine id</h2>
 * Two pods holding the same machine id at the same time will mint colliding ids, and pods
 * start and stop independently with no way to talk to each other directly. So
 * {@code machine_registry} ({@link com.gersimuca.erp.feature.machine_registry.MachineRegistryEntity}) is where they coordinate instead:
 * each pod atomically claims a free row on startup
 * ({@link com.gersimuca.erp.feature.machine_registry.MachineRegistryServiceImpl#allocateMachineId}), proves it's still alive with a
 * periodic heartbeat ({@link com.gersimuca.erp.common.machine_registry.MachineHeartbeatScheduler}), and a background sweep reclaims
 * rows from pods that stopped heartbeating - almost always because they crashed or were
 * killed ({@link com.gersimuca.erp.feature.machine_registry.MachineRegistryStaleCleanupServiceImpl}).
 *
 * <h2>Known limitations</h2>
 * Read these before changing capacity/throughput constants, or assuming stronger guarantees
 * than this actually provides:
 * <ul>
 *   <li><b>Cross-pod ordering is only as good as NTP.</b> Each pod stamps ids with its own
 *       clock; ids from two different pods are only loosely ordered relative to each other.</li>
 *   <li><b>Per-pod throughput ceiling</b> is {@code 2^SEQUENCE_BITS} ids/ms (see
 *       {@link com.gersimuca.erp.common.machine_registry.BusinessKeyLayout#SEQUENCE_BITS}). Exceeding it doesn't error - it silently
 *       borrows time from the next millisecond, and self-corrects once real time catches up.</li>
 *   <li><b>Capacity ceiling</b> is {@code 2^MACHINE_BITS} concurrently-live pods (see
 *       {@link com.gersimuca.erp.common.machine_registry.BusinessKeyLayout#MACHINE_BITS}), which has to cover rolling-update surge and
 *       any not-yet-reaped stale rows, not just steady-state pod count.</li>
 *   <li><b>Reallocation race window</b> - see the "known gap" note on
 *       {@link com.gersimuca.erp.common.machine_registry.MachineContext#getMachineId()}.</li>
 *   <li><b>Ungraceful death leaves a ghost row</b> for up to the stale-cleanup cutoff -
 *       {@code SIGKILL}, OOM-kill, and node eviction all skip {@link com.gersimuca.erp.common.machine_registry.MachineContext#release()}.</li>
 * </ul>
 */
package com.gersimuca.erp.common.machine_registry;

