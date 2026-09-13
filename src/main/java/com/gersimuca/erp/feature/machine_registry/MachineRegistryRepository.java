package com.gersimuca.erp.feature.machine_registry;

import java.time.Instant;

import com.gersimuca.erp.common.machine_registry.MachineContext;
import com.gersimuca.erp.common.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Direct, native/JPQL writes against {@code machine_registry} - deliberately not
 * {@code save()}/{@code deleteById()}, because Spring Data's generic {@code save()} maps to
 * Hibernate {@code merge()} whenever the entity's id is already set (as it always is here),
 * and {@code merge()} INSERTs-or-UPDATEs rather than INSERT-and-fail-on-duplicate. That would
 * let two pods "claim" the same machine id, with the second call silently overwriting the
 * first pod's row instead of throwing - exactly the collision this registry exists to
 * prevent. Every method below is written to fail loudly, or affect zero rows, when an
 * assumption about who owns what doesn't hold.
 */
public interface MachineRegistryRepository extends BaseRepository<MachineRegistryEntity, Long> {

    /**
     * Attempts to claim {@code machineId} for {@code instanceId} via a genuine SQL INSERT. If
     * another pod already holds this id, the primary-key constraint on
     * {@link MachineRegistryEntity#getMachineId()} rejects the INSERT and Spring translates it
     * to a {@link org.springframework.dao.DataIntegrityViolationException} - see
     * {@link MachineRegistryServiceImpl#allocateMachineId} for how that's caught and turned
     * into "try the next candidate".
     *
     * <p>Runs in {@code REQUIRES_NEW} rather than joining the caller's transaction: on most
     * databases (Postgres in particular) a failed statement aborts the entire enclosing
     * transaction, not just that statement. Without its own transaction, one failed candidate
     * in {@link MachineRegistryServiceImpl}'s retry loop would poison every subsequent attempt
     * in the same loop.
     *
     * @return always {@code 1} on success; failure to claim surfaces as an exception, never as
     *     a 0-row return
     */
    @Modifying(clearAutomatically = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query(
        value = """
          INSERT INTO machine_registry (machine_id, instance_id, service_name, allocated_at, last_heartbeat)
          VALUES (:machineId, :instanceId, :serviceName, :allocatedAt, :allocatedAt)
          """,
        nativeQuery = true)
    int tryClaim(
        @Param("machineId") Long machineId,
        @Param("instanceId") String instanceId,
        @Param("serviceName") String serviceName,
        @Param("allocatedAt") Instant allocatedAt);

    /**
     * Refreshes {@link MachineRegistryEntity#getLastHeartbeat()} for the row matching <b>both</b>
     * {@code machineId} and {@code instanceId}.
     *
     * <p>Matching on {@code instanceId} too - not just {@code machineId} - is what stops a pod
     * that has already lost this slot (reclaimed as stale, then claimed by a different pod)
     * from unknowingly refreshing the new owner's row and reporting a healthy heartbeat for a
     * slot it no longer holds. When that's happened, this matches zero rows, and the caller
     * ({@link MachineRegistryServiceImpl#updateHeartbeat}) treats that as "reallocate".
     *
     * @return {@code 1} if this instance still owns the slot, {@code 0} if it doesn't (or never did)
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(
        """
        UPDATE MachineRegistryEntity
        SET lastHeartbeat = :heartbeat
        WHERE machineId = :machineId AND instanceId = :instanceId
        """)
    int updateHeartbeat(
        @Param("machineId") Long machineId,
        @Param("instanceId") String instanceId,
        @Param("heartbeat") Instant heartbeat);

    /**
     * Deletes the row for {@code machineId} <b>only if</b> it still belongs to
     * {@code instanceId} - called on clean pod shutdown ({@link MachineContext#release()}).
     *
     * <p>Same reasoning as {@link #updateHeartbeat}, but the stakes are higher: without the
     * {@code instanceId} check, a pod shutting down cleanly could delete a *different*,
     * currently-live pod's registration - one that claimed this machine id after this pod's own
     * lease had already been reclaimed as stale - freeing a slot out from under a pod that's
     * still actively generating ids with it.
     *
     * @return {@code 1} if a row belonging to this instance was deleted, {@code 0} if this
     *     instance's lease was already reclaimed before shutdown ran (nothing to clean up)
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(
        """
        DELETE
        FROM MachineRegistryEntity
        WHERE machineId = :machineId AND instanceId = :instanceId
        """)
    int release(@Param("machineId") Long machineId, @Param("instanceId") String instanceId);

    /**
     * Deletes every row whose {@link MachineRegistryEntity#getLastHeartbeat()} is older than
     * {@code cutoff} - i.e. every pod that stopped proving it's alive, whether it crashed, was
     * killed without running {@link MachineContext#release()}, or is stuck badly enough that it
     * can't reach the database. Frees their machine ids for reuse. Driven by
     * {@link MachineRegistryStaleCleanupServiceImpl} on a fixed schedule.
     *
     * @return the number of stale rows removed, purely for logging
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(
        """
        DELETE
        FROM MachineRegistryEntity
        WHERE lastHeartbeat < :cutoff
        """)
    int cleanup(@Param("cutoff") Instant cutoff);
}
