package com.gersimuca.erp.common.machine_registry;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * The per-pod, per-logical-millisecond counter that disambiguates ids minted in the same tick of
 * {@link ClusterClock}. Immutable - every transition ({@link #next()}, {@link #reset()}) returns a
 * new instance rather than mutating this one.
 *
 * <p>Only {@link BusinessKeyGenerator} is expected to mutate "the current sequence" (by replacing
 * its reference - instances themselves never change) - see the class comment there for how {@link
 * #next()}, {@link #overflow()}, and {@link #reset()} fit together.
 */
public final class BusinessSequence {

  @Min(0)
  @Max(BusinessKeyLayout.MAX_SEQUENCE)
  private final Long value;

  public BusinessSequence(final Long value) {
    this.value = value;
  }

  public Long value() {
    return value;
  }

  /**
   * The next sequence value in the current logical millisecond. May exceed the valid range - always
   * check {@link #overflow()} after calling this, before encoding the result into an id.
   */
  public BusinessSequence next() {
    return new BusinessSequence(value + 1);
  }

  /** The first sequence value of a fresh logical millisecond. */
  public BusinessSequence reset() {
    return new BusinessSequence(0L);
  }

  /**
   * True once {@link #next()} has walked past {@link BusinessKeyLayout#MAX_SEQUENCE} - the signal
   * that the current logical millisecond has no sequence values left, and the caller must advance
   * to the next logical millisecond and {@link #reset()} before minting another id.
   */
  public Boolean overflow() {
    return value > BusinessKeyLayout.MAX_SEQUENCE;
  }
}
