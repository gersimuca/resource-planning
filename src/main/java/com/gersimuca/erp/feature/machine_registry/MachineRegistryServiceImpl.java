package com.gersimuca.erp.feature.machine_registry;

import com.gersimuca.erp.common.machine_registry.BusinessKeyLayout;
import com.gersimuca.erp.common.machine_registry.MachineId;
import com.gersimuca.erp.common.util.LoggerUtils;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * Default {@link MachineRegistryService}. Machine-id claiming is implemented as a
 * try-the-next-candidate loop rather than a single query, because there is no portable SQL
 * statement for "insert any row whose primary key isn't already taken" - the database's
 * primary-key constraint is what actually enforces exclusivity; this loop is just the
 * client-side retry around it.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MachineRegistryServiceImpl implements MachineRegistryService {

    private final MachineRegistryRepository repository;

    /**
     * How many machine-id slots are actually offered for allocation, {@code [0, maxMachineCount)}.
     * Defaults to the full structural capacity ({@link BusinessKeyLayout#MAX_MACHINE_ID} + 1).
     * Lower this via {@code machine-registry.max-machine-count} if you deliberately want a
     * smaller pool - it can never be raised past what {@link BusinessKeyLayout#MACHINE_BITS}
     * can represent; {@link #validateConfig()} enforces that at startup rather than letting it
     * fail confusingly later.
     */
    @Value("${machine-registry.max-machine-count:1024}")
    private int maxMachineCount;

    /**
     * Fails fast at startup if {@code machine-registry.max-machine-count} is set to something
     * the id layout can't actually represent, rather than letting a misconfiguration surface
     * later as silently-wrapped-around machine ids or a confusing allocation failure.
     */
    @PostConstruct
    void validateConfig() {
        final long structuralMax = BusinessKeyLayout.MAX_MACHINE_ID + 1;
        if (maxMachineCount < 1 || maxMachineCount > structuralMax) {
            throw new IllegalStateException(
                "machine-registry.max-machine-count (%d) must be between 1 and %d - the id format only reserves %d bits for the machine id"
                    .formatted(maxMachineCount, structuralMax, BusinessKeyLayout.MACHINE_BITS));
        }
    }

    @Override
    public MachineId allocateMachineId(final String instanceId, final String serviceName) {
        for (final long candidate : shuffledCandidates(maxMachineCount)) {
            try {
                repository.tryClaim(candidate, instanceId, serviceName, Instant.now());
                LoggerUtils.info(
                    log, "Allocated machineId={} to instanceId={} service={}", candidate, instanceId, serviceName);
                return new MachineId(candidate);
            } catch (DataIntegrityViolationException alreadyTaken) {
                // Someone else holds this candidate right now - not an error, just move on.
                // Caught deliberately broadly: any constraint violation on this INSERT, not only the
                // machine_id primary key, means "this row didn't go in", and the only sane response
                // either way is "try a different candidate".
            }
        }

        // Every candidate in [0, maxMachineCount) is currently claimed. This is a capacity
        // problem, not a transient one - retrying the same call immediately will fail again.
        throw new IllegalStateException(
            "Unable to allocate a machine id: all " + maxMachineCount + " slots are currently claimed");
    }

    @Override
    public boolean updateHeartbeat(final MachineId machineId, final String instanceId) {
        final int updated = repository.updateHeartbeat(machineId.value(), instanceId, Instant.now());
        if (updated == 0) {
            // Zero rows matched (machineId, instanceId) together - either this instance's lease was
            // already reclaimed as stale and handed to someone else, or (far less likely) it was
            // released elsewhere. Either way, this instance does not currently own a valid machine
            // id and must not keep minting ids with the one it thinks it has.
            LoggerUtils.warn(
                log,
                "Heartbeat for machineId={} instanceId={} matched no row - this instance no longer owns that slot (reclaimed as stale, or reassigned)",
                machineId.value(),
                instanceId);
        }
        return updated > 0;
    }

    @Override
    public void releaseMachineId(final MachineId machineId, final String instanceId) {
        final int released = repository.release(machineId.value(), instanceId);
        if (released == 0) {
            // Not an error: this instance's lease was already reclaimed by stale-cleanup before
            // shutdown got a chance to release it cleanly. Nothing left to delete.
            LoggerUtils.info(
                log,
                "Release for machineId={} instanceId={} matched no row - already reclaimed before shutdown, nothing to clean up",
                machineId.value(),
                instanceId);
        }
    }

    @Override
    public void cleanupInactiveMachines(final Instant cutOff) {
        final int removed = repository.cleanup(cutOff);
        if (removed > 0) {
            LoggerUtils.info(log, "Removed {} stale machine registry entries older than {}", removed, cutOff);
        }
    }

    /**
     * Candidates in random order so that many pods starting around the same time (a mass
     * rollout, a scale-up event) don't all try machine id 0 first, then 1, then 2 - which would
     * serialize their startup on a string of failed claims before spreading out. Shuffling gives
     * each pod a different, independent starting point instead.
     */
    private static List<Long> shuffledCandidates(final int count) {
        final List<Long> candidates = new ArrayList<>(count);
        for (long i = 0; i < count; i++) {
            candidates.add(i);
        }
        Collections.shuffle(candidates, ThreadLocalRandom.current());
        return candidates;
    }
}
