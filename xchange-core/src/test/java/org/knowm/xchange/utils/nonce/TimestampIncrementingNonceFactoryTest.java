package org.knowm.xchange.utils.nonce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class TimestampIncrementingNonceFactoryTest {

  @Test
  void createValue_returnsPositiveValue() {
    TimestampIncrementingNonceFactory factory = new TimestampIncrementingNonceFactory();
    Long value = factory.createValue();
    assertThat(value).isPositive();
  }

  @Test
  void createValue_increments() {
    TimestampIncrementingNonceFactory factory = new TimestampIncrementingNonceFactory();
    Long first = factory.createValue();
    Long second = factory.createValue();
    assertThat(second).isGreaterThan(first);
  }

  @Test
  void createValue_multipleIncrements() {
    TimestampIncrementingNonceFactory factory = new TimestampIncrementingNonceFactory();
    Long v1 = factory.createValue();
    Long v2 = factory.createValue();
    Long v3 = factory.createValue();
    Long v4 = factory.createValue();
    Long v5 = factory.createValue();
    assertThat(v1).isLessThan(v2);
    assertThat(v2).isLessThan(v3);
    assertThat(v3).isLessThan(v4);
    assertThat(v4).isLessThan(v5);
  }

  @Test
  void createValue_rapidCalls() {
    TimestampIncrementingNonceFactory factory = new TimestampIncrementingNonceFactory();
    long previous = 0;
    for (int i = 0; i < 100; i++) {
      long current = factory.createValue();
      assertThat(current).isGreaterThan(previous);
      previous = current;
    }
  }

  @Test
  void createValue_valuesAreReasonable() {
    // The start millis is Jan 1, 2013. Current time minus start time divided by 250 should
    // give a reasonable nonce value.
    TimestampIncrementingNonceFactory factory = new TimestampIncrementingNonceFactory();
    Long value = factory.createValue();
    long expectedMin = (System.currentTimeMillis() - 1356998400000L) / 250L;
    assertThat(value).isGreaterThanOrEqualTo(1);
    assertThat(value).isGreaterThanOrEqualTo(expectedMin);
  }
}
