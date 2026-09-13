package com.gersimuca.erp.common.machine_registry;

/**
 * The logical-clock value embedded in the "timestamp" bits of a generated id: milliseconds
 * since {@link ClusterClock}'s epoch. Immutable - {@link #next()} returns a new instance
 * rather than mutating this one.
 *
 * <p>"Logical" because {@link BusinessKeyGenerator} never lets this value fall behind its
 * own previous value, even if the underlying wall clock briefly moves backwards (an NTP
 * correction, a leap second, a paused VM resuming). It only ever holds still or advances -
 * see {@link BusinessKeyGenerator#nextId()} for exactly when each happens.
 *
 * <p>Not a substitute for a real audit or event timestamp: this value can run ahead of true
 * wall-clock time under sustained load (see {@link BusinessKeyLayout#SEQUENCE_BITS}), and is
 * only comparable across different pods to whatever precision their clocks are kept in sync
 * by NTP - it is not a strict cross-machine ordering guarantee.
 */
public final class BusinessTimestamp {

    private final Long value;

    public BusinessTimestamp(final Long value) {
        this.value = value;
    }

    public Long value() {
        return value;
    }

    /**
     * One logical millisecond after this one - used to force the clock forward when
     * {@link BusinessSequence#overflow()} exhausts the current tick.
     */
    public BusinessTimestamp next() {
        return new BusinessTimestamp(value + 1);
    }
}
