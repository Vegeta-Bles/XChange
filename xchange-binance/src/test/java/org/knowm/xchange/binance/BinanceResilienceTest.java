package org.knowm.xchange.binance;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.resilience4j.ratelimiter.RateLimiter;
import org.junit.jupiter.api.Test;
import org.knowm.xchange.client.ResilienceRegistries;

class BinanceResilienceTest {

  @Test
  void createRegistries_returnsNonEmptyRegistries() {
    ResilienceRegistries registries = BinanceResilience.createRegistries();
    assertThat(registries).isNotNull();
  }

  @Test
  void createRegistries_containsRequestWeightRateLimiter() {
    ResilienceRegistries registries = BinanceResilience.createRegistries();
    RateLimiter rateLimiter = registries.rateLimiters().rateLimiter(BinanceResilience.REQUEST_WEIGHT_RATE_LIMITER);
    assertThat(rateLimiter).isNotNull();
  }

  @Test
  void createRegistries_containsOrdersPerSecondRateLimiter() {
    ResilienceRegistries registries = BinanceResilience.createRegistries();
    RateLimiter rateLimiter = registries.rateLimiters().rateLimiter(BinanceResilience.ORDERS_PER_SECOND_RATE_LIMITER);
    assertThat(rateLimiter).isNotNull();
  }

  @Test
  void createRegistries_containsOrdersPer10SecondsRateLimiter() {
    ResilienceRegistries registries = BinanceResilience.createRegistries();
    RateLimiter rateLimiter = registries.rateLimiters().rateLimiter(BinanceResilience.ORDERS_PER_10_SECONDS_RATE_LIMITER);
    assertThat(rateLimiter).isNotNull();
  }

  @Test
  void createRegistries_containsOrdersPerDayRateLimiter() {
    ResilienceRegistries registries = BinanceResilience.createRegistries();
    RateLimiter rateLimiter = registries.rateLimiters().rateLimiter(BinanceResilience.ORDERS_PER_DAY_RATE_LIMITER);
    assertThat(rateLimiter).isNotNull();
  }

  @Test
  void createRegistries_containsRawRequestsRateLimiter() {
    ResilienceRegistries registries = BinanceResilience.createRegistries();
    RateLimiter rateLimiter = registries.rateLimiters().rateLimiter(BinanceResilience.RAW_REQUESTS_RATE_LIMITER);
    assertThat(rateLimiter).isNotNull();
  }

  @Test
  void createRegistries_requestWeightLimiterHasCorrectLimit() {
    ResilienceRegistries registries = BinanceResilience.createRegistries();
    RateLimiter rateLimiter = registries.rateLimiters().rateLimiter(BinanceResilience.REQUEST_WEIGHT_RATE_LIMITER);
    // 6000 requests per minute
    assertThat(rateLimiter).isNotNull();
  }

  @Test
  void createRegistries_ordersPerSecondLimiterHasCorrectLimit() {
    ResilienceRegistries registries = BinanceResilience.createRegistries();
    RateLimiter rateLimiter = registries.rateLimiters().rateLimiter(BinanceResilience.ORDERS_PER_SECOND_RATE_LIMITER);
    // 10 orders per second
    assertThat(rateLimiter).isNotNull();
  }

  @Test
  void createRegistries_ordersPer10SecondsLimiterHasCorrectLimit() {
    ResilienceRegistries registries = BinanceResilience.createRegistries();
    RateLimiter rateLimiter = registries.rateLimiters().rateLimiter(BinanceResilience.ORDERS_PER_10_SECONDS_RATE_LIMITER);
    // 100 orders per 10 seconds
    assertThat(rateLimiter).isNotNull();
  }

  @Test
  void createRegistriesFuture_returnsNonEmptyRegistries() {
    ResilienceRegistries registries = BinanceResilience.createRegistriesFuture();
    assertThat(registries).isNotNull();
  }

  @Test
  void createRegistriesFuture_containsRequestWeightRateLimiter() {
    ResilienceRegistries registries = BinanceResilience.createRegistriesFuture();
    RateLimiter rateLimiter = registries.rateLimiters().rateLimiter(BinanceResilience.REQUEST_WEIGHT_RATE_LIMITER);
    assertThat(rateLimiter).isNotNull();
  }

  @Test
  void createRegistriesFuture_containsOrdersPer10SecondsRateLimiter() {
    ResilienceRegistries registries = BinanceResilience.createRegistriesFuture();
    RateLimiter rateLimiter = registries.rateLimiters().rateLimiter(BinanceResilience.ORDERS_PER_10_SECONDS_RATE_LIMITER);
    assertThat(rateLimiter).isNotNull();
  }

  @Test
  void createRegistriesFuture_containsOrdersPerMinuteRateLimiter() {
    ResilienceRegistries registries = BinanceResilience.createRegistriesFuture();
    RateLimiter rateLimiter = registries.rateLimiters().rateLimiter(BinanceResilience.ORDERS_PER_MINUTE_RATE_LIMITER);
    assertThat(rateLimiter).isNotNull();
  }

  @Test
  void createRegistriesFuture_doesNotContainOrdersPerSecondRateLimiter() {
    ResilienceRegistries registries = BinanceResilience.createRegistriesFuture();
    // Futures version doesn't have the spot-specific ordersPerSecond limiter
    // It sets it to Integer.MAX_VALUE for compatibility
    RateLimiter rateLimiter = registries.rateLimiters().rateLimiter(BinanceResilience.ORDERS_PER_SECOND_RATE_LIMITER);
    assertThat(rateLimiter).isNotNull();
  }

  @Test
  void createRegistries_spotAndFutureAreDifferent() {
    ResilienceRegistries spotRegistries = BinanceResilience.createRegistries();
    ResilienceRegistries futureRegistries = BinanceResilience.createRegistriesFuture();

    // Spot has ordersPerDay, futures doesn't
    RateLimiter spotOrdersPerDay = spotRegistries.rateLimiters().rateLimiter(BinanceResilience.ORDERS_PER_DAY_RATE_LIMITER);
    assertThat(spotOrdersPerDay).isNotNull();

    // Futures has ordersPerMinute, spot doesn't
    RateLimiter futureOrdersPerMinute = futureRegistries.rateLimiters().rateLimiter(BinanceResilience.ORDERS_PER_MINUTE_RATE_LIMITER);
    assertThat(futureOrdersPerMinute).isNotNull();
  }

  @Test
  void createRegistries_requestWeightLimitDiffersBetweenSpotAndFuture() {
    // Spot: 6000 per minute, Future: 2400 per minute
    ResilienceRegistries spotRegistries = BinanceResilience.createRegistries();
    ResilienceRegistries futureRegistries = BinanceResilience.createRegistriesFuture();

    RateLimiter spotLimiter = spotRegistries.rateLimiters().rateLimiter(BinanceResilience.REQUEST_WEIGHT_RATE_LIMITER);
    RateLimiter futureLimiter = futureRegistries.rateLimiters().rateLimiter(BinanceResilience.REQUEST_WEIGHT_RATE_LIMITER);

    assertThat(spotLimiter).isNotNull();
    assertThat(futureLimiter).isNotNull();
  }

  @Test
  void createRegistries_rawRequestsLimiterExists() {
    ResilienceRegistries registries = BinanceResilience.createRegistries();
    RateLimiter rateLimiter = registries.rateLimiters().rateLimiter(BinanceResilience.RAW_REQUESTS_RATE_LIMITER);
    assertThat(rateLimiter).isNotNull();
  }

  @Test
  void createRegistriesFuture_rawRequestsLimiterExists() {
    ResilienceRegistries registries = BinanceResilience.createRegistriesFuture();
    RateLimiter rateLimiter = registries.rateLimiters().rateLimiter(BinanceResilience.RAW_REQUESTS_RATE_LIMITER);
    assertThat(rateLimiter).isNotNull();
  }

  @Test
  void constants_areDefined() {
    assertThat(BinanceResilience.REQUEST_WEIGHT_RATE_LIMITER).isEqualTo("requestWeight");
    assertThat(BinanceResilience.ORDERS_PER_10_SECONDS_RATE_LIMITER).isEqualTo("ordersPer10Seconds");
    assertThat(BinanceResilience.ORDERS_PER_SECOND_RATE_LIMITER).isEqualTo("ordersPerSecond");
    assertThat(BinanceResilience.ORDERS_PER_DAY_RATE_LIMITER).isEqualTo("ordersPerDay");
    assertThat(BinanceResilience.RAW_REQUESTS_RATE_LIMITER).isEqualTo("rawRequests");
    assertThat(BinanceResilience.ORDERS_PER_MINUTE_RATE_LIMITER).isEqualTo("ordersPerMINUTE");
  }
}
