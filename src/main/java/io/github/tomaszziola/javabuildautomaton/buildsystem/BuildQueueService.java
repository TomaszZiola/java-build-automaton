package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static java.util.concurrent.TimeUnit.SECONDS;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@Service
@EnableAsync
public class BuildQueueService {

  private final BuildService buildService;
  private final BlockingQueue<Long> queue = new LinkedBlockingQueue<>(100);
  private static final Logger LOGGER = LoggerFactory.getLogger(BuildQueueService.class);

  public BuildQueueService(final BuildService buildService) {
    this.buildService = buildService;
  }

  public void enqueue(final Long buildId) {
    final boolean offered = queue.offer(buildId);
    if (offered) {
      LOGGER.info("Enqueued build id={}", buildId);
    } else {
      LOGGER.warn("Queue full, dropping build id={}", buildId);
    }
  }

  @Async
  @PostConstruct
  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  void runWorker() {
    LOGGER.info("Build worker started");
    while (true) {
      try {
        final Long buildId = queue.poll(1, SECONDS);
        if (buildId == null) {
          continue;
        }
        buildService.executeBuild(buildId);
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
        return;
      } catch (Exception ex) {
        LOGGER.error("Worker error", ex);
      }
    }
  }
}
