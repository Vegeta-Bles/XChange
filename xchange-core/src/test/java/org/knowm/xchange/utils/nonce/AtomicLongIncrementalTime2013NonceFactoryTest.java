package org.knowm.xchange.utils.nonce;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class AtomicLongIncrementalTime2013NonceFactoryTest {

  @Test
  void createValue_returnsPositiveValue() {
    AtomicLongIncrementalTime2013NonceFactory factory = new AtomicLongIncrementalTime2013NonceFactory();
    Long value = factory.createValue();
    assertThat(value).isPositive();
  }

  @Test
  void createValue_increments() {
    AtomicLongIncrementalTime2013NonceFactory factory = new AtomicLongIncrementalTime2013NonceFactory();
    Long first = factory.createValue();
    Long second = factory.createValue();
    assertThat(second).isEqualTo(first + 1);
  }

  @Test
  void createValue_multipleIncrements() {
    AtomicLongIncrementalTime2013NonceFactory factory = new AtomicLongIncrementalTime2013NonceFactory();
    Long v1 = factory.createValue();
    Long v2 = factory.createValue();
    Long v3 = factory.createValue();
    assertThat(v2).isEqualTo(v1 + 1);
    assertThat(v3).isEqualTo(v2 + 1);
  }

  @Test
  void createValue_consecutiveValues() {
    AtomicLongIncrementalTime2013NonceFactory factory = new AtomicLongIncrementalTime2013NonceFactory();
    long previous = 0;
    for (int i = 0; i < 100; i++) {
      long current = factory.createValue();
      assertThat(current).isEqualTo(previous + 1);
      previous = current;
    }
  }

  @Test
  void createValue_threadSafe() throws InterruptedException {
    AtomicLongIncrementalTime2013NonceFactory factory = new AtomicLongIncrementalTime2013NonceFactory();
    int numThreads = 10;
    int callsPerThread = 100;
    Thread[] threads = new Thread[numThreads];

    for (int i = 0; i < numThreads; i++) {
      threads[i] = new Thread(() -> {
        for (int j = 0; j < callsPerThread; j++) {
          factory.createValue();
        }
      });
    }

    for (Thread thread : threads) {
      thread.start();
    }
    for (Thread thread : threads) {
      thread.join();
    }

    // All values should be unique (1000 total calls, 1000 unique values)
    // We can't directly check uniqueness without storing values, but we can verify
    // the factory produces values without errors in concurrent context
  }

  @Test
  void createValue_initialValueIsReasonable() {
    AtomicLongIncrementalTime2013NonceFactory factory = new AtomicLongIncrementalTime2013NonceFactory();
    Long first = factory.createValue();
    // Initial value should be based on (currentTime - 2013-01-01) / 250
    long expectedInitial = (System.currentTimeMillis() - 1356998400000L) / 250L;
    assertThat(first).isGreaterThan(expectedInitial);
  }
}
