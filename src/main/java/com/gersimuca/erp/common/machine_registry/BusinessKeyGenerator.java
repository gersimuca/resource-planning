package com.gersimuca.erp.common.machine_registry;

import com.gersimuca.erp.feature.machine_registry.MachineRegistryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mints Snowflake-style 64-bit ids: a logical timestamp, this pod's leased machine id ({@link
 * MachineContext}), and a per-millisecond sequence, packed together according to {@link
 * BusinessKeyLayout}.
 *
 * <p>Uniqueness rests on one invariant: at any instant, at most one pod holds any given machine id
 * (enforced by {@link MachineRegistryService} and everything under it). Given that, two ids can
 * only collide if they share <em>all three</em> fields - impossible by construction, since {@link
 * #sequence} is bumped or {@link #logicalTimestamp} is advanced on every single call, for every
 * possible outcome of comparing the current time to the last-used one.
 *
 * <p>{@code synchronized} because {@link #logicalTimestamp} and {@link #sequence} are mutable,
 * shared, sequential state - every {@code nextId()} call must see the previous call's result before
 * deciding its own, or two threads could both read the same starting state and produce the same
 * sequence value. This serializes id generation on a single pod behind one lock; not a real
 * bottleneck given the throughput ceiling below is already far lower than what an uncontended lock
 * could sustain.
 */
@Component
@RequiredArgsConstructor
public class BusinessKeyGenerator {

  /**
   * Sequence occupies the lowest bits, so it needs no shift of its own once {@code encode} starts
   * from the right.
   */
  private static final int MACHINE_SHIFT = BusinessKeyLayout.SEQUENCE_BITS;

  /**
   * Machine id sits above the sequence, timestamp above both - see {@link BusinessKeyLayout}'s
   * class comment for the full picture.
   */
  private static final int TIME_SHIFT =
      BusinessKeyLayout.MACHINE_BITS + BusinessKeyLayout.SEQUENCE_BITS;

  private final MachineContext machineContext;
  private final ClusterClock clock;

  /**
   * The logical millisecond of the most recently generated id. Never moves backwards - see the
   * "never regress" branch below.
   */
  private BusinessTimestamp logicalTimestamp = new BusinessTimestamp(0L);

  /** The sequence value of the most recently generated id within {@link #logicalTimestamp}. */
  private BusinessSequence sequence = new BusinessSequence(0L);

  /**
   * Produces the next id for this pod. Every call falls into exactly one of two cases:
   *
   * <ol>
   *   <li><b>The wall clock has moved past the last logical tick</b> ({@code current >
   *       logicalTimestamp}): adopt the new time and start the sequence over at 0. This is the
   *       overwhelmingly common case under normal load.
   *   <li><b>It hasn't</b> (same millisecond, or the wall clock briefly went backwards - an NTP
   *       correction, a leap second, a paused VM resuming): stay in the current logical tick and
   *       advance the sequence instead. If the sequence has no room left ({@link
   *       BusinessSequence#overflow()}) - meaning {@link BusinessKeyLayout#SEQUENCE_BITS} worth of
   *       ids have already been minted in this tick - force the logical clock forward by one and
   *       reset the sequence, effectively borrowing a millisecond that hasn't happened yet. This is
   *       what "the logical clock can drift ahead of the wall clock under sustained overload" (see
   *       {@link BusinessKeyLayout#SEQUENCE_BITS}) actually looks like in code - it self-corrects
   *       once real time catches back up.
   * </ol>
   *
   * Either way, {@link #logicalTimestamp} only ever holds still or increases - it is never allowed
   * to move backwards, which is what keeps ids strictly increasing per pod even across a wall-clock
   * regression.
   */
  public synchronized Long nextId() {
    final BusinessTimestamp current = clock.now();

    if (current.value() > logicalTimestamp.value()) {
      logicalTimestamp = current;
      sequence = sequence.reset();
    } else {
      sequence = sequence.next();
      if (Boolean.TRUE.equals(sequence.overflow())) {
        logicalTimestamp = logicalTimestamp.next();
        sequence = sequence.reset();
      }
    }

    return encode(logicalTimestamp, machineContext.getMachineId(), sequence);
  }

  /**
   * Packs the three fields into one {@code long} via the shifts {@link BusinessKeyLayout} defines:
   * timestamp highest, then machine id, then sequence in the lowest bits.
   */
  private Long encode(
      final BusinessTimestamp timestamp, final Long machineId, final BusinessSequence sequence) {
    return (timestamp.value() << TIME_SHIFT) | (machineId << MACHINE_SHIFT) | sequence.value();
  }
}
