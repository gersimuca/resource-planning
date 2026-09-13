package com.gersimuca.erp.feature.machine_registry;

import com.gersimuca.erp.common.machine_registry.BusinessKeyLayout;
import com.gersimuca.erp.common.machine_registry.MachineContext;
import com.gersimuca.erp.common.machine_registry.MachineHeartbeatScheduler;
import com.gersimuca.erp.common.machine_registry.MachineRegistryStaleCleanupServiceImpl;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

/**
 * One row = one leased machine id. While a row for a given {@link #machineId} exists, that value is
 * considered owned and must not be handed to any other pod - see {@link
 * MachineRegistryServiceImpl#allocateMachineId} for how that's enforced at the database level.
 *
 * <p>A row is removed in exactly two ways: the owning pod releases it on clean shutdown ({@link
 * MachineContext#release()}), or {@link MachineRegistryStaleCleanupServiceImpl} reclaims it after
 * {@link #lastHeartbeat} goes stale (the pod likely crashed or was killed without a chance to
 * release cleanly).
 */
@Entity
@Table(name = "machine_registry")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
public class MachineRegistryEntity {

  /**
   * The leased machine id itself (0 to {@link BusinessKeyLayout#MAX_MACHINE_ID}) - also the primary
   * key, which is what makes a second pod's INSERT for the same value fail with a primary-key
   * violation instead of silently succeeding. That failure is exactly the signal {@link
   * MachineRegistryServiceImpl#allocateMachineId} depends on to move to the next candidate.
   *
   * <p>Deliberately not {@code @GeneratedValue}: the value is chosen by the allocator, not the
   * database, since it must fit within {@link BusinessKeyLayout#MACHINE_BITS}.
   */
  @Id
  @Column(name = "machine_id", nullable = false)
  private Long machineId;

  /**
   * A fresh {@link java.util.UUID} minted once per pod ({@link MachineContext#getInstanceId()}),
   * identifying *which pod* currently holds {@link #machineId}. Every write that touches an
   * existing row (heartbeat, release) filters on this alongside {@link #machineId}, so a pod that
   * no longer owns a slot - because it was reclaimed as stale and reassigned to someone else - can
   * never accidentally refresh or delete the new owner's row. See {@link
   * MachineRegistryRepository#updateHeartbeat} and {@link MachineRegistryRepository#release}.
   */
  @Column(name = "instance_id", nullable = false, unique = true)
  private String instanceId;

  /**
   * Which logical service this pod belongs to (e.g. {@code spring.application.name}) -
   * informational, for telling rows apart when inspecting the table by hand; no allocation or
   * ownership logic depends on it.
   */
  @Column(name = "service_name", nullable = false, length = 100)
  private String serviceName;

  /**
   * When this pod claimed {@link #machineId}. Never updated after insert - purely informational.
   */
  @Column(name = "allocated_at", nullable = false, updatable = false)
  private Instant allocatedAt;

  /**
   * Last time this pod proved it was still alive (see {@link MachineHeartbeatScheduler}). {@link
   * MachineRegistryStaleCleanupServiceImpl} deletes any row whose heartbeat is older than its
   * cutoff, freeing that machine id for reuse - so a pod that stops heartbeating (crash, prolonged
   * GC pause, network partition) eventually loses its lease even though it never explicitly
   * released it.
   */
  @Column(name = "last_heartbeat")
  private Instant lastHeartbeat;
}
