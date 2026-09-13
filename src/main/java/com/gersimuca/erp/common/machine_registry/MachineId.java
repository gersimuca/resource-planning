package com.gersimuca.erp.common.machine_registry;

import com.gersimuca.erp.feature.machine_registry.MachineRegistryServiceImpl;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * A machine id: the slot in {@code machine_registry} this pod currently owns, and the value
 * embedded in the "machine id" bits of every id it generates (see {@link BusinessKeyGenerator}).
 *
 * <p>Immutable value object - a change of machine id (e.g. after {@link
 * MachineContext#reallocate()}) always produces a new instance rather than mutating an existing
 * one, so nothing can hold a stale reference and observe it silently change underneath it.
 *
 * <p>The valid range ({@code 0} to {@link BusinessKeyLayout#MAX_MACHINE_ID}) is enforced by bean
 * validation here, and independently enforced at the allocation boundary in {@link
 * MachineRegistryServiceImpl#validateConfig()} - the latter guards against misconfiguration before
 * any id is ever allocated; this annotation is a second line of defense on the value itself.
 */
public final class MachineId {

  @Min(0)
  @Max(BusinessKeyLayout.MAX_MACHINE_ID)
  private final Long value;

  public MachineId(final Long value) {
    this.value = value;
  }

  public Long value() {
    return value;
  }
}
