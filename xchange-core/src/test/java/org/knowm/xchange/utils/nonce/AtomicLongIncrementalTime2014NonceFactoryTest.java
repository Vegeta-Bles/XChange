package org.knowm.xchange.utils.nonce;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AtomicLongIncrementalTime2014NonceFactoryTest {

  @Test
  void createValue_returnsPositiveValue() {
    AtomicLongIncrementalTime2014NonceFactory factory = new AtomicLongIncrementalTime2014NonceFactory();
    Long value = factory.createValue();
    assertThat(value).isPositive();
  }

  @Test
  void createValue_increments() {
    AtomicLongIncrementalTime2014NonceFactory factory = new AtomicLongIncrementalTime2014NonceFactory();
    Long first = factory.createValue();
    Long second = factory.createValue();
    assertThat(second).isEqualTo(first + 1);
  }

  @Test
  void createValue_multipleIncrements() {
    AtomicLongIncrementalTime2014NonceFactory factory = new AtomicLongIncrementalTime2014NonceFactory();
    Long v1 = factory.createValue();
    Long v2 = factory.createValue();
    Long v3 = factory.createValue();
    assertThat(v2).isEqualTo(v1 + 1);
    assertThat(v3).isEqualTo(v2 + 1);
  }

  @Test
  void createValue_consecutiveValues() {
    AtomicLongIncrementalTime2014NonceFactory factory = new AtomicLongIncrementalTime2014NonceFactory();
    long previous = 0;
    for (int i = 0; i < 50; i++) {
      long current = factory.createValue();
      assertThat(current).isEqualTo(previous + 1);
      previous = current;
    }
  }

  @Test
  void createValue_initialValueIsReasonable() {
    AtomicLongIncrementalTime2014NonceFactory factory = new AtomicLongIncrementalTime2014NonceFactory();
    Long first = factory.createValue();
    // Initial value should be based on (currentTime - 2014-01-01) / 250
    long expectedInitial = (System.currentTimeMillis() - 1388534400000L) / 250L;
    assertThat(first).isGreaterThan(expectedInitial);
  }

  @Test
  void createValue_2014StartIsLaterThan2013() {
    // The 2014 factory should start from a later base than the 2013 factory
    AtomicLongIncrementalTime2014NonceFactory factory2014 = new AtomicLongIncrementalTime2014NonceFactory();
    AtomicLongIncrementalTime2013NonceFactory factory2013 = new AtomicLongIncrementalTime2013NonceFactory();

    Long v2014 = factory2014.createValue();
    Long v2013 = factory2013.createValue();

    // 2013 factory should have a higher initial value since it starts from an earlier date
    assertThat(v2013).isGreaterThan(v2014);
  }
}
