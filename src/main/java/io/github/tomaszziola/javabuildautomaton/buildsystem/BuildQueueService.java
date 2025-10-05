package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("PMD.CloseResource")
public class BuildQueueService {

  private static final Logger LOGGER = LoggerFactory.getLogger(BuildQueueService.class);
  private final Semaphore permits;
  private final BlockingQueue<Long> queue;
  private final AtomicBoolean started = new AtomicBoolean(false);

  private volatile ExecutorService buildExecutor;
  private volatile ExecutorService workerExecutor;

  private final BuildService buildService;

  public BuildQueueService(final BuildService buildService, final BuildProperties props) {
    this.buildService = buildService;
    this.permits = new Semaphore(props.getMaxParallel(), true);
    this.queue = new LinkedBlockingQueue<>(props.getQueue().getCapacity());
  }

  public void enqueue(final Long buildId) {
    if (buildId == null) {
      LOGGER.warn("Cannot enqueue null build id");
      return;
    }
    final boolean offered = queue.offer(buildId);
    if (offered) {
      LOGGER.info("Enqueued build id={}", buildId);
    } else {
      LOGGER.warn("Queue full, dropping build id={}", buildId);
    }
  }

  @EventListener(ApplicationReadyEvent.class)
  void startWorker() {
    if (!started.compareAndSet(false, true)) {
      LOGGER.info("Build worker already started, skipping");
      return;
    }
    workerExecutor =
        newSingleThreadExecutor(
            r -> {
              final var thread = new Thread(r, "build-worker");
              thread.setDaemon(false);
              return thread;
            });
    buildExecutor = newVirtualThreadPerTaskExecutor();

    try {
      workerExecutor.submit(this::runWorkerLoop);
      LOGGER.info("Build worker scheduled");
    } catch (RejectedExecutionException rex) {
      LOGGER.error("Failed to schedule worker loop", rex);
    }
  }

  void runWorkerLoop() {
    LOGGER.info("Build worker started");
    while (!currentThread().isInterrupted()) {
      try {
        final var buildId = queue.poll(1, SECONDS);
        if (buildId == null) {
          continue;
        }
        permits.acquire();
        try {
          buildExecutor.submit(
              () -> {
                try {
                  buildService.executeBuild(buildId);
                } finally {
                  permits.release();
                }
              });
        } catch (RejectedExecutionException rex) {
          LOGGER.error("Build executor rejected task for id={}", buildId, rex);
          permits.release();
          SECONDS.sleep(1);
        }
      } catch (InterruptedException ie) {
        currentThread().interrupt();
        break;
      }
    }
    LOGGER.info("Build worker stopped");
  }

  @PreDestroy
  @SuppressWarnings("PMD.NullAssignment")
  void stopWorker() {
    LOGGER.info("Shutting down build worker");
    final ExecutorService workerExec = workerExecutor;
    final ExecutorService buildExec = buildExecutor;

    if (workerExec != null) {
      workerExec.shutdownNow();
      try {
        if (!workerExec.awaitTermination(10, SECONDS)) {
          LOGGER.warn("Worker executor did not terminate in time");
        }
      } catch (InterruptedException e) {
        currentThread().interrupt();
      }
    }
    if (buildExec != null) {
      buildExec.shutdown();
      try {
        if (!buildExec.awaitTermination(60, SECONDS)) {
          LOGGER.warn("Build executor did not terminate in time, forcing shutdown");
          buildExec.shutdownNow();
        }
      } catch (InterruptedException e) {
        currentThread().interrupt();
      }
    }

    workerExecutor = null;
    buildExecutor = null;
    started.set(false);
  }
}
