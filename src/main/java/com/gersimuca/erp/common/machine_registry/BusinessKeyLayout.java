package com.gersimuca.erp.common.machine_registry;

import com.gersimuca.erp.feature.machine_registry.MachineRegistryServiceImpl;

/**
 * The single source of truth for how a generated id's 64 bits are divided between
 * timestamp, machine id, and sequence.
 *
 * <p>This is a <b>structural</b> decision, not a runtime tuning knob. Every id ever
 * generated encodes its meaning according to whatever these constants were at the moment
 * it was minted. If you change {@link #MACHINE_BITS} or {@link #SEQUENCE_BITS} after ids
 * are already in the wild:
 * <ul>
 *   <li>Old and new ids are no longer comparable by numeric value in a way that respects
 *       chronological order - the bit boundaries have moved.</li>
 *   <li>Anything that decodes an id back into (timestamp, machineId, sequence) using the
 *       old shift amounts will decode newly-generated ids incorrectly, and vice versa.</li>
 * </ul>
 * Treat a change here the same way you'd treat a database schema migration: it needs a
 * coordinated rollout, not just a config change.
 *
 * <p>Current layout, most significant bit first:
 * <pre>
 *   [ 47 bits: logical timestamp ][ 10 bits: machine id ][ 6 bits: sequence ]
 * </pre>
 * 47, not 48, because the result is a signed {@code long}: bit 63 is the sign bit, so only
 * 63 bits are available for a value that must stay non-negative, and
 * {@code 63 - MACHINE_BITS - SEQUENCE_BITS = 47}. That's still roughly 4,460 years of
 * headroom from {@link ClusterClock}'s epoch before the timestamp field grows large enough
 * to flip the sign bit and produce a negative id - not a practical concern today, but worth
 * knowing if {@link #MACHINE_BITS} or {@link #SEQUENCE_BITS} ever grow.
 */
public final class BusinessKeyLayout {

    /**
     * Bits reserved for the machine id. Governs the maximum number of pods that can hold a
     * distinct machine id at the same time: {@code 2^MACHINE_BITS} slots total.
     *
     * <p>10 bits = 1024 slots. Sized with headroom above "60+ pods" to absorb OpenShift
     * rolling-update surge (extra pods briefly running alongside the ones they're replacing)
     * and any rows still waiting on stale-cleanup to be reclaimed - both eat into the same
     * pool as steady-state pods. See {@link MachineRegistryServiceImpl#maxMachineCount} to cap
     * usage below the structural maximum without changing this layout.
     */
    public static final int MACHINE_BITS = 10;

    /**
     * Bits reserved for the per-millisecond sequence counter. Governs the maximum ids a single
     * pod can mint inside one logical millisecond before {@link BusinessSequence#overflow()}
     * forces the logical clock forward: {@code 2^SEQUENCE_BITS} ids/ms/pod.
     *
     * <p>6 bits = 64 ids/ms/pod, i.e. ~64,000 ids/sec/pod. This is a hard ceiling per pod,
     * not an error condition - see {@link BusinessKeyGenerator} for what happens when it's hit.
     */
    public static final int SEQUENCE_BITS = 6;

    /** Largest machine id the current layout can represent: {@code 2^MACHINE_BITS - 1}. */
    public static final long MAX_MACHINE_ID = (1L << MACHINE_BITS) - 1;

    /** Largest sequence value the current layout can represent: {@code 2^SEQUENCE_BITS - 1}. */
    public static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;

    private BusinessKeyLayout() {
        // constants holder - not instantiable
    }
}
