package org.knowm.xchange.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.knowm.xchange.client.ResilienceUtils.matchesHttpCode;
import static org.knowm.xchange.client.ResilienceUtils.CallableApi.wrapCallable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.vavr.control.Either;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.concurrent.Callable;
import org.junit.jupiter.api.Test;
import org.knowm.xchange.ExchangeSpecification;
import si.mazi.rescu.HttpStatusExceptionSupport;

class ResilienceUtilsTest {

  @Test
  void decorateApiCall_returnsDecorateCallableApi() {
    ExchangeSpecification.ResilienceSpecification resilienceSpec =
        new ExchangeSpecification.ResilienceSpecification();
    ResilienceUtils.CallableApi<String> callable = () -> "test";

    ResilienceUtils.DecorateCallableApi<String> decorated =
        ResilienceUtils.decorateApiCall(resilienceSpec, callable);

    assertThat(decorated).isNotNull();
  }

  @Test
  void decorateApiCall_withRetryEnabled() throws IOException {
    ExchangeSpecification.ResilienceSpecification resilienceSpec =
        new ExchangeSpecification.ResilienceSpecification();
    resilienceSpec.setRetryEnabled(true);

    Retry retry = Retry.ofDefaults("test");
    ResilienceUtils.CallableApi<String> callable = () -> "success";

    ResilienceUtils.DecorateCallableApi<String> decorated =
        ResilienceUtils.decorateApiCall(resilienceSpec, callable).withRetry(retry);

    String result = decorated.call();
    assertThat(result).isEqualTo("success");
  }

  @Test
  void decorateApiCall_withRetryDisabled() throws IOException {
    ExchangeSpecification.ResilienceSpecification resilienceSpec =
        new ExchangeSpecification.ResilienceSpecification();
    resilienceSpec.setRetryEnabled(false);

    Retry retry = Retry.ofDefaults("test");
    ResilienceUtils.CallableApi<String> callable = () -> "success";

    ResilienceUtils.DecorateCallableApi<String> decorated =
        ResilienceUtils.decorateApiCall(resilienceSpec, callable).withRetry(retry);

    String result = decorated.call();
    assertThat(result).isEqualTo("success");
  }

  @Test
  void decorateApiCall_withRateLimiterEnabled() throws IOException {
    ExchangeSpecification.ResilienceSpecification resilienceSpec =
        new ExchangeSpecification.ResilienceSpecification();
    resilienceSpec.setRateLimiterEnabled(true);

    RateLimiter rateLimiter = RateLimiter.of("test", RateLimiterConfig.defaultConfig());
    ResilienceUtils.CallableApi<String> callable = () -> "success";

    ResilienceUtils.DecorateCallableApi<String> decorated =
        ResilienceUtils.decorateApiCall(resilienceSpec, callable).withRateLimiter(rateLimiter);

    String result = decorated.call();
    assertThat(result).isEqualTo("success");
  }

  @Test
  void decorateApiCall_withRateLimiterDisabled() throws IOException {
    ExchangeSpecification.ResilienceSpecification resilienceSpec =
        new ExchangeSpecification.ResilienceSpecification();
    resilienceSpec.setRateLimiterEnabled(false);

    RateLimiter rateLimiter = RateLimiter.of("test", RateLimiterConfig.defaultConfig());
    ResilienceUtils.CallableApi<String> callable = () -> "success";

    ResilienceUtils.DecorateCallableApi<String> decorated =
        ResilienceUtils.decorateApiCall(resilienceSpec, callable).withRateLimiter(rateLimiter);

    String result = decorated.call();
    assertThat(result).isEqualTo("success");
  }

  @Test
  void decorateApiCall_withRateLimiterWithPermits() throws IOException {
    ExchangeSpecification.ResilienceSpecification resilienceSpec =
        new ExchangeSpecification.ResilienceSpecification();
    resilienceSpec.setRateLimiterEnabled(true);

    RateLimiter rateLimiter = RateLimiter.of("test", RateLimiterConfig.defaultConfig());
    ResilienceUtils.CallableApi<String> callable = () -> "success";

    ResilienceUtils.DecorateCallableApi<String> decorated =
        ResilienceUtils.decorateApiCall(resilienceSpec, callable).withRateLimiter(rateLimiter, 5);

    String result = decorated.call();
    assertThat(result).isEqualTo("success");
  }

  @Test
  void decorateApiCall_callableThrowsIOException() {
    ExchangeSpecification.ResilienceSpecification resilienceSpec =
        new ExchangeSpecification.ResilienceSpecification();

    ResilienceUtils.CallableApi<String> callable = () -> {
      throw new IOException("test exception");
    };

    ResilienceUtils.DecorateCallableApi<String> decorated =
        ResilienceUtils.decorateApiCall(resilienceSpec, callable);

    assertThatThrownBy(() -> decorated.call())
        .isInstanceOf(IOException.class)
        .hasMessage("test exception");
  }

  @Test
  void matchesHttpCode_rightSide_returnsFalse() {
    Either<Throwable, String> either = Either.right("success");
    assertThat(matchesHttpCode(either, Response.Status.OK)).isFalse();
  }

  @Test
  void matchesHttpCode_wrongStatus_returnsFalse() {
    HttpStatusExceptionSupport exception = mock(HttpStatusExceptionSupport.class);
    when(exception.getHttpStatusCode()).thenReturn(404);
    Either<Throwable, String> either = Either.left(exception);
    assertThat(matchesHttpCode(either, Response.Status.OK)).isFalse();
  }

  @Test
  void matchesHttpCode_correctStatus_returnsTrue() {
    HttpStatusExceptionSupport exception = mock(HttpStatusExceptionSupport.class);
    when(exception.getHttpStatusCode()).thenReturn(404);
    Either<Throwable, String> either = Either.left(exception);
    assertThat(matchesHttpCode(either, Response.Status.NOT_FOUND)).isTrue();
  }

  @Test
  void matchesHttpCode_notHttpStatusException_returnsFalse() {
    RuntimeException exception = new RuntimeException("not http");
    Either<Throwable, String> either = Either.left(exception);
    assertThat(matchesHttpCode(either, Response.Status.OK)).isFalse();
  }

  @Test
  void matchesHttpCode_allHttpStatusCodes() {
    // Test various HTTP status codes
    assertThat(matchesHttpCode(Either.<Throwable, Object>left(createHttpStatusException(200)), Response.Status.OK)).isTrue();
    assertThat(matchesHttpCode(Either.<Throwable, Object>left(createHttpStatusException(201)), Response.Status.CREATED)).isTrue();
    assertThat(matchesHttpCode(Either.<Throwable, Object>left(createHttpStatusException(400)), Response.Status.BAD_REQUEST)).isTrue();
    assertThat(matchesHttpCode(Either.<Throwable, Object>left(createHttpStatusException(401)), Response.Status.UNAUTHORIZED)).isTrue();
    assertThat(matchesHttpCode(Either.<Throwable, Object>left(createHttpStatusException(403)), Response.Status.FORBIDDEN)).isTrue();
    assertThat(matchesHttpCode(Either.<Throwable, Object>left(createHttpStatusException(404)), Response.Status.NOT_FOUND)).isTrue();
    assertThat(matchesHttpCode(Either.<Throwable, Object>left(createHttpStatusException(500)), Response.Status.INTERNAL_SERVER_ERROR)).isTrue();
    assertThat(matchesHttpCode(Either.<Throwable, Object>left(createHttpStatusException(503)), Response.Status.SERVICE_UNAVAILABLE)).isTrue();
  }

  @Test
  void wrapCallable_wrapsSuccessfully() {
    Callable<String> callable = () -> "wrapped";
    ResilienceUtils.CallableApi<String> wrapped = wrapCallable(callable);
    assertThat(wrapped.call()).isEqualTo("wrapped");
  }

  @Test
  void wrapCallable_wrapsIOException() {
    Callable<String> callable = () -> {
      throw new IOException("io error");
    };
    ResilienceUtils.CallableApi<String> wrapped = wrapCallable(callable);
    assertThatThrownBy(() -> wrapped.call())
        .isInstanceOf(IOException.class);
  }

  @Test
  void wrapCallable_wrapsRuntimeException() {
    Callable<String> callable = () -> {
      throw new RuntimeException("runtime error");
    };
    ResilienceUtils.CallableApi<String> wrapped = wrapCallable(callable);
    assertThatThrownBy(() -> wrapped.call())
        .isInstanceOf(RuntimeException.class)
        .hasMessage("runtime error");
  }

  @Test
  void wrapCallable_wrapsCheckedExceptionAsIllegalState() {
    Callable<String> callable = () -> {
      throw new Exception("checked error");
    };
    ResilienceUtils.CallableApi<String> wrapped = wrapCallable(callable);
    assertThatThrownBy(() -> wrapped.call())
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void decorateApiCall_chainedWithRetryAndRateLimiter() throws IOException {
    ExchangeSpecification.ResilienceSpecification resilienceSpec =
        new ExchangeSpecification.ResilienceSpecification();
    resilienceSpec.setRetryEnabled(true);
    resilienceSpec.setRateLimiterEnabled(true);

    Retry retry = Retry.ofDefaults("test");
    RateLimiter rateLimiter = RateLimiter.of("test", RateLimiterConfig.defaultConfig());
    ResilienceUtils.CallableApi<String> callable = () -> "chained";

    ResilienceUtils.DecorateCallableApi<String> decorated =
        ResilienceUtils.decorateApiCall(resilienceSpec, callable)
            .withRetry(retry)
            .withRateLimiter(rateLimiter);

    String result = decorated.call();
    assertThat(result).isEqualTo("chained");
  }

  @Test
  void decorateApiCall_callableApiInterface() {
    ResilienceUtils.CallableApi<String> callable = () -> "interface test";
    assertThat(callable.call()).isEqualTo("interface test");
  }

  @Test
  void decorateApiCall_decorateCallableApiCall() throws IOException {
    ExchangeSpecification.ResilienceSpecification resilienceSpec =
        new ExchangeSpecification.ResilienceSpecification();
    ResilienceUtils.CallableApi<String> callable = () -> "direct call";

    ResilienceUtils.DecorateCallableApi<String> decorated =
        ResilienceUtils.decorateApiCall(resilienceSpec, callable);

    String result = decorated.call();
    assertThat(result).isEqualTo("direct call");
  }

  private HttpStatusExceptionSupport createHttpStatusException(int statusCode) {
    HttpStatusExceptionSupport exception = mock(HttpStatusExceptionSupport.class);
    when(exception.getHttpStatusCode()).thenReturn(statusCode);
    return exception;
  }
}
