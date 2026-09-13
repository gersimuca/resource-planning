package com.gersimuca.erp.feature.machine_registry;

import com.gersimuca.erp.common.machine_registry.MachineContext;
import com.gersimuca.erp.common.machine_registry.MachineHeartbeatScheduler;
import com.gersimuca.erp.common.machine_registry.MachineId;

import java.time.Instant;

/**
 * Coordinates machine-id leases across every pod in the cluster, backed by
 * {@code machine_registry} ({@link MachineRegistryEntity}). This is the layer that turns
 * "give me a machine id" into a distributed-locking problem and solves it with the database
 * as the single source of truth for who currently owns what.
 *
 * @see MachineContext for the per-pod object that calls this on startup and holds the result
 * @see MachineHeartbeatScheduler for what keeps a lease alive
 * @see MachineRegistryStaleCleanupService for how abandoned leases are reclaimed
 */
public interface MachineRegistryService {

    /**
     * Claims a free machine id for the calling pod. Tries every candidate in
     * {@code [0, maxMachineCount)} in random order until one succeeds; a candidate "fails" when
     * another pod already holds it (see {@link MachineRegistryRepository#tryClaim}).
     *
     * @param instanceId this pod's unique identity ({@link MachineContext#getInstanceId()}) -
     *     recorded on the claimed row so later heartbeats/release can prove ownership
     * @param serviceName the logical service this pod belongs to; informational only
     * @return a machine id this pod now exclusively owns, until it's released or reclaimed
     * @throws IllegalStateException if every candidate is currently claimed by another pod -
     *     callers should treat this as "the cluster is at capacity", not a transient error to
     *     blindly retry
     */
    MachineId allocateMachineId(String instanceId, String serviceName);

    /**
     * Proves {@code instanceId} is still alive and still wants to hold {@code machineId}.
     *
     * @return {@code true} if the lease is still held after this call; {@code false} if it had
     *     already been reclaimed (e.g. by {@link #cleanupInactiveMachines}) and reassigned to a
     *     different pod before this heartbeat arrived - callers must treat {@code false} as
     *     "you need a new machine id", not as a no-op
     */
    boolean updateHeartbeat(MachineId machineId, String instanceId);

    /**
     * Gives up {@code machineId} on behalf of {@code instanceId}, freeing it immediately for
     * another pod rather than waiting out the stale-cleanup cutoff. Only removes a row that
     * still belongs to {@code instanceId} - a no-op if the lease was already reclaimed as stale
     * before this could run (see {@link MachineRegistryRepository#release}).
     *
     * <p>Only reliable on a clean shutdown path ({@link MachineContext#release()}, driven by
     * {@code @PreDestroy}) - a hard kill of the process skips this entirely, which is exactly
     * what {@link #cleanupInactiveMachines} exists to eventually clean up instead.
     */
    void releaseMachineId(MachineId machineId, String instanceId);

    /**
     * Removes every lease whose last heartbeat is older than {@code cutOff}, regardless of
     * which pod holds it - the safety net for pods that disappeared without releasing cleanly.
     */
    void cleanupInactiveMachines(Instant cutOff);
}
