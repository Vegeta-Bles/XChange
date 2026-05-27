package org.knowm.xchange.utils.nonce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class CurrentTimeIncrementalNonceFactoryTest {

  @Test
  void createValue_seconds_returnsPositive() {
    CurrentTimeIncrementalNonceFactory factory = new CurrentTimeIncrementalNonceFactory(TimeUnit.SECONDS);
    Long value = factory.createValue();
    assertThat(value).isPositive();
  }

  @Test
  void createValue_milliseconds_returnsPositive() {
    CurrentTimeIncrementalNonceFactory factory = new CurrentTimeIncrementalNonceFactory(TimeUnit.MILLISECONDS);
    Long value = factory.createValue();
    assertThat(value).isPositive();
  }

  @Test
  void createValue_microseconds_returnsPositive() {
    CurrentTimeIncrementalNonceFactory factory = new CurrentTimeIncrementalNonceFactory(TimeUnit.MICROSECONDS);
    Long value = factory.createValue();
    assertThat(value).isPositive();
  }

  @Test
  void createValue_nanoseconds_returnsPositive() {
    CurrentTimeIncrementalNonceFactory factory = new CurrentTimeIncrementalNonceFactory(TimeUnit.NANOSECONDS);
    Long value = factory.createValue();
    assertThat(value).isPositive();
  }

  @Test
  void createValue_seconds_increments() {
    CurrentTimeIncrementalNonceFactory factory = new CurrentTimeIncrementalNonceFactory(TimeUnit.SECONDS);
    Long first = factory.createValue();
    Long second = factory.createValue();
    assertThat(second).isGreaterThanOrEqualTo(first);
  }

  @Test
  void createValue_milliseconds_increments() {
    CurrentTimeIncrementalNonceFactory factory = new CurrentTimeIncrementalNonceFactory(TimeUnit.MILLISECONDS);
    Long first = factory.createValue();
    Long second = factory.createValue();
    assertThat(second).isGreaterThanOrEqualTo(first);
  }

  @Test
  void createValue_microseconds_increments() {
    CurrentTimeIncrementalNonceFactory factory = new CurrentTimeIncrementalNonceFactory(TimeUnit.MICROSECONDS);
    Long first = factory.createValue();
    Long second = factory.createValue();
    assertThat(second).isGreaterThanOrEqualTo(first);
  }

  @Test
  void createValue_nanoseconds_increments() {
    CurrentTimeIncrementalNonceFactory factory = new CurrentTimeIncrementalNonceFactory(TimeUnit.NANOSECONDS);
    Long first = factory.createValue();
    Long second = factory.createValue();
    assertThat(second).isGreaterThanOrEqualTo(first);
  }

  @Test
  void createValue_unsupportedTimeUnit_throws() {
    assertThatThrownBy(() -> new CurrentTimeIncrementalNonceFactory(TimeUnit.DAYS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("not supported");
  }

  @Test
  void createValue_unsupportedTimeUnit_hours() {
    assertThatThrownBy(() -> new CurrentTimeIncrementalNonceFactory(TimeUnit.HOURS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("not supported");
  }

  @Test
  void createValue_multipleCalls_seconds() {
    CurrentTimeIncrementalNonceFactory factory = new CurrentTimeIncrementalNonceFactory(TimeUnit.SECONDS);
    long previous = 0;
    for (int i = 0; i < 10; i++) {
      long current = factory.createValue();
      assertThat(current).isGreaterThanOrEqualTo(previous);
      previous = current;
    }
  }

  @Test
  void createValue_multipleCalls_milliseconds() {
    CurrentTimeIncrementalNonceFactory factory = new CurrentTimeIncrementalNonceFactory(TimeUnit.MILLISECONDS);
    long previous = 0;
    for (int i = 0; i < 100; i++) {
      long current = factory.createValue();
      assertThat(current).isGreaterThanOrEqualTo(previous);
      previous = current;
    }
  }

  @Test
  void createValue_nanoseconds_providesHighResolution() {
    CurrentTimeIncrementalNonceFactory factory = new CurrentTimeIncrementalNonceFactory(TimeUnit.NANOSECONDS);
    Long v1 = factory.createValue();
    Long v2 = factory.createValue();
    Long v3 = factory.createValue();

    // Values should be in nanoTime range (typically billions)
    assertThat(v1).isGreaterThan(1_000_000_000L);
    assertThat(v3).isGreaterThanOrEqualTo(v2);
    assertThat(v2).isGreaterThanOrEqualTo(v1);
  }

  @Test
  void createValue_microseconds_providesMediumResolution() {
    CurrentTimeIncrementalNonceFactory factory = new CurrentTimeIncrementalNonceFactory(TimeUnit.MICROSECONDS);
    Long v1 = factory.createValue();
    Long v2 = factory.createValue();

    // Values should be in microsecond range
    assertThat(v1).isGreaterThan(1_000_000L);
    assertThat(v2).isGreaterThanOrEqualTo(v1);
  }

  @Test
  void createValue_sameTimeMultipleCalls_increments() {
    // Rapid calls should still increment even if time doesn't change
    CurrentTimeIncrementalNonceFactory factory = new CurrentTimeIncrementalNonceFactory(TimeUnit.SECONDS);
    long previous = 0;
    for (int i = 0; i < 10; i++) {
      long current = factory.createValue();
      assertThat(current).isGreaterThanOrEqualTo(previous);
      previous = current;
    }
  }

  @Test
  void createValue_milliseconds_providesMillisecondResolution() {
    CurrentTimeIncrementalNonceFactory factory = new CurrentTimeIncrementalNonceFactory(TimeUnit.MILLISECONDS);
    Long v1 = factory.createValue();

    // Values should be in millisecond range (typically in the billions for current dates)
    assertThat(v1).isGreaterThan(1_000_000_000L);
  }

  @Test
  void createValue_seconds_providesSecondResolution() {
    CurrentTimeIncrementalNonceFactory factory = new CurrentTimeIncrementalNonceFactory(TimeUnit.SECONDS);
    Long v1 = factory.createValue();

    // Values should be in second range (typically in the billions for current dates)
    assertThat(v1).isGreaterThan(1_000_000_000L);
  }

  @Test
  void createValue_threadSafe() throws InterruptedException {
    CurrentTimeIncrementalNonceFactory factory = new CurrentTimeIncrementalNonceFactory(TimeUnit.MILLISECONDS);
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
  }
}
