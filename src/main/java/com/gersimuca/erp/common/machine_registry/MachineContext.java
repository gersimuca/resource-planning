package com.gersimuca.erp.common.machine_registry;

import com.gersimuca.erp.common.util.LoggerUtils;
import com.gersimuca.erp.feature.machine_registry.MachineRegistryRepository;
import com.gersimuca.erp.feature.machine_registry.MachineRegistryService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This pod's machine identity for the lifetime of the JVM: a stable {@link #instanceId}
 * generated once at construction, and a {@link #machineId} leased from
 * {@link MachineRegistryService} on startup and held until shutdown (or until
 * {@link #reallocate()} is forced by a lost lease - see {@link MachineHeartbeatScheduler}).
 *
 * <p>{@link BusinessKeyGenerator} reads {@link #getMachineId()} on every single call to
 * {@code nextId()}, so this class sits on the hot path for id generation even though it does
 * almost no work itself - it's just a place for the currently-owned id to live where the
 * generator can see updates to it.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MachineContext {

    private final MachineRegistryService registryService;

    /**
     * This pod's logical service name, recorded on its registry row for humans inspecting the
     * table - not used in any ownership check. Comes from {@code spring.application.name}
     * rather than a hardcoded string because this class lives in a shared {@code common}
     * package: every service that reuses it needs its own value here.
     */
    @Value("${spring.application.name}")
    private String serviceName;

    /**
     * A fresh random id, generated once when this bean is constructed and never changed for the
     * life of the JVM. This is what proves ownership of a registry row across heartbeats and
     * release (see {@link MachineRegistryRepository#updateHeartbeat} and
     * {@link MachineRegistryRepository#release}) - unlike {@link #machineId}, this value is
     * never reassigned, even across a {@link #reallocate()}.
     */
    @Getter
    private final String instanceId = UUID.randomUUID().toString();

    /**
     * The machine id currently leased to this pod, or {@code null} before {@link #init()} has
     * completed. {@code volatile} because it's written by whichever thread runs
     * {@link #reallocate()} (initial startup, or later the heartbeat scheduler's thread) and
     * read by every request thread calling {@link BusinessKeyGenerator#nextId()} - without
     * {@code volatile}, a reallocation on one thread would not be guaranteed to become visible
     * to id generation happening concurrently on others.
     *
     * <p><b>Known gap:</b> there is a brief window during a {@link #reallocate()} where a
     * concurrent {@code nextId()} call can still read the <em>old</em> value after this pod has
     * already lost that lease (detected by {@link MachineHeartbeatScheduler}, but not yet
     * replaced by a fresh {@link MachineRegistryService#allocateMachineId} call completing).
     * Fully closing that means pausing id generation for the moment reallocation happens, which
     * this implementation does not do - reallocation is expected only after a real anomaly
     * (missed heartbeats), not as a normal event, so the window is rare in practice, not
     * eliminated in principle.
     */
    @Getter
    private volatile Long machineId;

    /**
     * Claims this pod's first machine id. Failure here (see
     * {@link MachineRegistryService#allocateMachineId}) fails application startup - by design: a
     * pod that can't get a machine id must not come up and start generating ids with none.
     */
    @PostConstruct
    public void init() {
        reallocate();
    }

    /**
     * Claims a brand new machine id and swaps it in, replacing whatever this pod held before
     * (if anything). Called once at startup via {@link #init()}, and again later by
     * {@link MachineHeartbeatScheduler} if a heartbeat reveals this pod's lease was reclaimed
     * out from under it.
     *
     * <p>{@code synchronized} so two callers (say, a slow startup racing an early heartbeat)
     * can't both allocate concurrently and leave {@link #machineId} pointing at whichever call
     * happened to finish last while the other call's claimed row leaks, unreleased, in the
     * registry.
     */
    public synchronized void reallocate() {
        final MachineId allocated = registryService.allocateMachineId(instanceId, serviceName);
        this.machineId = allocated.value();
        LoggerUtils.info(log, "Allocated machineId={} to instanceId={} service={}", machineId, instanceId, serviceName);
    }

    /**
     * Gives up this pod's lease on clean shutdown, so the slot is available to another pod
     * immediately rather than sitting occupied until stale-cleanup's cutoff elapses.
     *
     * <p>{@code @PreDestroy} only runs on a graceful shutdown path. {@code SIGKILL}, an
     * out-of-memory kill, or a node eviction all skip this entirely - those cases are left to
     * {@link MachineRegistryStaleCleanupServiceImpl} to notice and clean up later. For that
     * safety net to actually help during a rolling deploy on OpenShift, the deployment's
     * {@code terminationGracePeriodSeconds} needs to give this method enough time to run before
     * a forced kill.
     */
    @PreDestroy
    public void release() {
        final Long id = machineId;
        if (id != null) {
            LoggerUtils.info(log, "Releasing machineId={} for instanceId={} on shutdown", id, instanceId);
            registryService.releaseMachineId(new MachineId(id), instanceId);
        }
    }
}
