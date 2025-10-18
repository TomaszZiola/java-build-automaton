package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static ch.qos.logback.classic.Level.TRACE;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.TooManyMethods")
class BuildQueueServiceTest extends BaseUnit {

  private ListAppender<ILoggingEvent> logAppender;

  @BeforeEach
  void setUp() {
    final Logger logger = (Logger) LoggerFactory.getLogger(BuildQueueService.class);
    logAppender = new ListAppender<>();
    logAppender.start();
    logger.addAppender(logAppender);
    logger.setLevel(TRACE);
  }

  @AfterEach
  void tearDown() {
    assertDoesNotThrow(this::execute);
  }

  @Test
  @DisplayName("Given application ready event, when starting worker, then worker is scheduled")
  void startsWorkerOnApplicationReady() {
    // when
    assertDoesNotThrow(buildQueueServiceImpl::startWorker);

    // then
    assertThat(
            logAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(m -> m.contains("Build worker scheduled")))
        .isTrue();
  }

  @Test
  @DisplayName(
      "Given worker already started, when starting worker again, then skips duplicate start")
  void skipsStartingWorkerWhenAlreadyStarted() {
    // given
    buildQueueServiceImpl.startWorker();

    // when
    assertDoesNotThrow(buildQueueServiceImpl::startWorker);

    // then
    assertThat(
            logAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(m -> m.contains("Build worker already started")))
        .isTrue();
  }

  @Test
  @DisplayName("Given worker running, when shutting down, then worker stops gracefully")
  void stopsWorkerOnShutdown() {
    // given
    buildQueueServiceImpl.startWorker();

    // when
    assertDoesNotThrow(buildQueueServiceImpl::stopWorker);

    // then
    assertThat(
            logAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(m -> m.contains("Build worker stopped")))
        .isTrue();
  }

  @Test
  @DisplayName(
      "Given build service throws exception, when executing build, then exception is handled")
  void handlesExecutionExceptions() throws Exception {
    // given
    doThrow(new RuntimeException("Build execution failed")).when(buildService).execute(buildId);
    buildQueueServiceImpl.startWorker();

    // when
    buildQueueServiceImpl.enqueue(buildId);

    sleep(150);

    // then
    verify(buildService, times(1)).execute(buildId);
    assertDoesNotThrow(buildQueueServiceImpl::stopWorker);
  }

  @Test
  @DisplayName(
      "Given multiple builds, when processing concurrently, then respects max parallel limit")
  void respectsMaxParallelLimit() throws Exception {
    // given
    final AtomicInteger current = new AtomicInteger();
    final AtomicInteger max = new AtomicInteger();
    final CountDownLatch latch = new CountDownLatch(3);
    doAnswer(
            inv -> {
              final int incremented = current.incrementAndGet();
              max.accumulateAndGet(incremented, Math::max);
              try {
                sleep(200);
              } finally {
                current.decrementAndGet();
                latch.countDown();
              }
              return null;
            })
        .when(buildService)
        .execute(anyLong());

    buildQueueServiceImpl.startWorker();

    // when
    buildQueueServiceImpl.enqueue(1L);
    buildQueueServiceImpl.enqueue(2L);
    buildQueueServiceImpl.enqueue(3L);

    // then
    assertThat(latch.await(2, SECONDS)).isTrue();
    assertThat(max.get() <= buildProperties.getMaxParallel()).isTrue();
  }

  @Test
  @DisplayName("Given null build id, when enqueuing, then handles gracefully")
  void handlesNullBuildId() {
    // when
    assertDoesNotThrow(() -> buildQueueServiceImpl.enqueue(null));

    // then
    verifyNoInteractions(buildService);
  }

  @Test
  @DisplayName("Given worker not started, when shutting down, then handles gracefully")
  void handlesShutdownWhenNotStarted() {
    // when & then
    assertDoesNotThrow(buildQueueServiceImpl::stopWorker);
  }

  @Test
  @DisplayName(
      "Given worker thread interrupted during queue polling, when processing, then worker loop exits gracefully")
  void handlesInterruptedExceptionInWorkerLoop() throws Exception {
    // given
    buildQueueServiceImpl.startWorker();

    sleep(50);

    // when
    buildQueueServiceImpl.stopWorker();

    // then
    assertThat(
            logAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(m -> m.contains("Build worker stopped")))
        .isTrue();
  }

  @Test
  @DisplayName(
      "Given semaphore acquisition during high load, when processing multiple builds, then handles concurrent access correctly")
  void handlesSemaphoreAcquisitionUnderLoad() throws Exception {
    // given
    buildProperties.setMaxParallel(1);
    buildProperties.getQueue().setCapacity(5);

    buildQueueServiceImpl = new BuildQueueService(buildService, buildProperties);

    final CountDownLatch latch = new CountDownLatch(3);
    doAnswer(
            inv -> {
              latch.countDown();
              return null;
            })
        .when(buildService)
        .execute(anyLong());

    buildQueueServiceImpl.startWorker();

    // when
    for (int i = 1; i <= 3; i++) {
      buildQueueServiceImpl.enqueue((long) i);
    }

    // then
    assertThat(latch.await(1, SECONDS)).isTrue();
    verify(buildService, times(3)).execute(anyLong());
  }

  @Test
  @DisplayName("Given worker stopped, when starting again, then restart succeeds")
  void restartsWorkerAfterShutdown() throws Exception {
    // given
    buildQueueServiceImpl.startWorker();

    // when
    buildQueueServiceImpl.stopWorker();

    // then
    assertDoesNotThrow(buildQueueServiceImpl::startWorker);
    sleep(50);
    assertDoesNotThrow(buildQueueServiceImpl::stopWorker);
  }

  @Test
  @DisplayName("Given full queue, when enqueuing, then drop and log warning")
  void dropsWhenQueueFull() {
    // given
    buildProperties.getQueue().setCapacity(1);
    buildQueueServiceImpl = new BuildQueueService(buildService, buildProperties);

    // when
    buildQueueServiceImpl.enqueue(1L);
    buildQueueServiceImpl.enqueue(2L);

    // then
    assertThat(
            logAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(m -> m.contains("Queue full")))
        .isTrue();
  }

  private void execute() {
    buildQueueServiceImpl.stopWorker();
  }
}
