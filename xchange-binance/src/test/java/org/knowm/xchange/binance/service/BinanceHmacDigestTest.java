package org.knowm.xchange.binance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.QueryParam;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import org.junit.jupiter.api.Test;
import si.mazi.rescu.Params;
import si.mazi.rescu.RestInvocation;

class BinanceHmacDigestTest {

  private static final String SECRET_KEY_BASE64 = "test-secret-key";

  @Test
  void createInstance_withNullKey_returnsNull() {
    assertThat(BinanceHmacDigest.createInstance(null)).isNull();
  }

  @Test
  void createInstance_withValidKey_returnsDigest() {
    BinanceHmacDigest digest = BinanceHmacDigest.createInstance(SECRET_KEY_BASE64);
    assertThat(digest).isNotNull();
  }

  @Test
  void digestParams_getRequest() throws Exception {
    BinanceHmacDigest digest = BinanceHmacDigest.createInstance(SECRET_KEY_BASE64);
    RestInvocation invocation = mock(RestInvocation.class);
    when(invocation.getHttpMethod()).thenReturn("GET");

    Params params = new Params();
    params.add("symbol", "BTCUSDT");
    params.add("timestamp", "1234567890");

    RestInvocation.HttpParamsMap paramsMap = mock(RestInvocation.HttpParamsMap.class);
    RestInvocation.HttpHeaders headers = mock(RestInvocation.HttpHeaders.class);
    when(headers.asHttpHeaders()).thenReturn(java.util.Map.of("symbol", "BTCUSDT", "timestamp", "1234567890"));
    when(paramsMap.get(QueryParam.class)).thenReturn(headers);
    when(invocation.getParamsMap()).thenReturn(paramsMap);
    when(invocation.getRequestBody()).thenReturn("");
    when(invocation.getPath()).thenReturn("/api/v3/order");

    String signature = digest.digestParams(invocation);
    assertThat(signature).isNotNull();
    assertThat(signature).isNotEmpty();
    assertThat(signature).hasSize(64); // SHA-256 produces 64 hex characters
  }

  @Test
  void digestParams_postRequest() throws Exception {
    BinanceHmacDigest digest = BinanceHmacDigest.createInstance(SECRET_KEY_BASE64);
    RestInvocation invocation = mock(RestInvocation.class);
    when(invocation.getHttpMethod()).thenReturn("POST");

    RestInvocation.HttpParamsMap paramsMap = mock(RestInvocation.HttpParamsMap.class);
    RestInvocation.HttpHeaders headers = mock(RestInvocation.HttpHeaders.class);
    when(headers.asHttpHeaders()).thenReturn(java.util.Map.of("symbol", "BTCUSDT", "timestamp", "1234567890"));
    when(paramsMap.get(QueryParam.class)).thenReturn(headers);
    when(invocation.getParamsMap()).thenReturn(paramsMap);
    when(invocation.getRequestBody()).thenReturn("{\"quantity\":\"0.1\"}");
    when(invocation.getPath()).thenReturn("/api/v3/order");

    String signature = digest.digestParams(invocation);
    assertThat(signature).isNotNull();
    assertThat(signature).isNotEmpty();
    assertThat(signature).hasSize(64);
  }

  @Test
  void digestParams_deleteRequest() throws Exception {
    BinanceHmacDigest digest = BinanceHmacDigest.createInstance(SECRET_KEY_BASE64);
    RestInvocation invocation = mock(RestInvocation.class);
    when(invocation.getHttpMethod()).thenReturn("DELETE");

    RestInvocation.HttpParamsMap paramsMap = mock(RestInvocation.HttpParamsMap.class);
    RestInvocation.HttpHeaders headers = mock(RestInvocation.HttpHeaders.class);
    when(headers.asHttpHeaders()).thenReturn(java.util.Map.of("symbol", "BTCUSDT", "timestamp", "1234567890"));
    when(paramsMap.get(QueryParam.class)).thenReturn(headers);
    when(invocation.getParamsMap()).thenReturn(paramsMap);
    when(invocation.getRequestBody()).thenReturn("");
    when(invocation.getPath()).thenReturn("/api/v3/order");

    String signature = digest.digestParams(invocation);
    assertThat(signature).isNotNull();
    assertThat(signature).isNotEmpty();
    assertThat(signature).hasSize(64);
  }

  @Test
  void digestParams_putRequest() throws Exception {
    BinanceHmacDigest digest = BinanceHmacDigest.createInstance(SECRET_KEY_BASE64);
    RestInvocation invocation = mock(RestInvocation.class);
    when(invocation.getHttpMethod()).thenReturn("PUT");

    RestInvocation.HttpParamsMap paramsMap = mock(RestInvocation.HttpParamsMap.class);
    RestInvocation.HttpHeaders headers = mock(RestInvocation.HttpHeaders.class);
    when(headers.asHttpHeaders()).thenReturn(java.util.Map.of("symbol", "BTCUSDT", "timestamp", "1234567890"));
    when(paramsMap.get(QueryParam.class)).thenReturn(headers);
    when(invocation.getParamsMap()).thenReturn(paramsMap);
    when(invocation.getRequestBody()).thenReturn("{\"quantity\":\"0.2\"}");
    when(invocation.getPath()).thenReturn("/api/v3/order");

    String signature = digest.digestParams(invocation);
    assertThat(signature).isNotNull();
    assertThat(signature).isNotEmpty();
    assertThat(signature).hasSize(64);
  }

  @Test
  void digestParams_differentKeys_produceDifferentSignatures() throws Exception {
    BinanceHmacDigest digest1 = BinanceHmacDigest.createInstance("secret-key-1");
    BinanceHmacDigest digest2 = BinanceHmacDigest.createInstance("secret-key-2");

    RestInvocation invocation = mock(RestInvocation.class);
    when(invocation.getHttpMethod()).thenReturn("GET");

    RestInvocation.HttpParamsMap paramsMap = mock(RestInvocation.HttpParamsMap.class);
    RestInvocation.HttpHeaders headers = mock(RestInvocation.HttpHeaders.class);
    when(headers.asHttpHeaders()).thenReturn(java.util.Map.of("symbol", "BTCUSDT"));
    when(paramsMap.get(QueryParam.class)).thenReturn(headers);
    when(invocation.getParamsMap()).thenReturn(paramsMap);
    when(invocation.getRequestBody()).thenReturn("");
    when(invocation.getPath()).thenReturn("/api/v3/order");

    String sig1 = digest1.digestParams(invocation);
    String sig2 = digest2.digestParams(invocation);

    assertThat(sig1).isNotEqualTo(sig2);
  }

  @Test
  void digestParams_differentParams_produceDifferentSignatures() throws Exception {
    BinanceHmacDigest digest = BinanceHmacDigest.createInstance(SECRET_KEY_BASE64);

    RestInvocation invocation1 = mock(RestInvocation.class);
    when(invocation1.getHttpMethod()).thenReturn("GET");
    RestInvocation.HttpParamsMap paramsMap1 = mock(RestInvocation.HttpParamsMap.class);
    RestInvocation.HttpHeaders headers1 = mock(RestInvocation.HttpHeaders.class);
    when(headers1.asHttpHeaders()).thenReturn(java.util.Map.of("symbol", "BTCUSDT"));
    when(paramsMap1.get(QueryParam.class)).thenReturn(headers1);
    when(invocation1.getParamsMap()).thenReturn(paramsMap1);
    when(invocation1.getRequestBody()).thenReturn("");
    when(invocation1.getPath()).thenReturn("/api/v3/order");

    RestInvocation invocation2 = mock(RestInvocation.class);
    when(invocation2.getHttpMethod()).thenReturn("GET");
    RestInvocation.HttpParamsMap paramsMap2 = mock(RestInvocation.HttpParamsMap.class);
    RestInvocation.HttpHeaders headers2 = mock(RestInvocation.HttpHeaders.class);
    when(headers2.asHttpHeaders()).thenReturn(java.util.Map.of("symbol", "ETHUSDT"));
    when(paramsMap2.get(QueryParam.class)).thenReturn(headers2);
    when(invocation2.getParamsMap()).thenReturn(paramsMap2);
    when(invocation2.getRequestBody()).thenReturn("");
    when(invocation2.getPath()).thenReturn("/api/v3/order");

    String sig1 = digest.digestParams(invocation1);
    String sig2 = digest.digestParams(invocation2);

    assertThat(sig1).isNotEqualTo(sig2);
  }

  @Test
  void digestParams_signatureIsHex() throws Exception {
    BinanceHmacDigest digest = BinanceHmacDigest.createInstance(SECRET_KEY_BASE64);
    RestInvocation invocation = mock(RestInvocation.class);
    when(invocation.getHttpMethod()).thenReturn("GET");

    RestInvocation.HttpParamsMap paramsMap = mock(RestInvocation.HttpParamsMap.class);
    RestInvocation.HttpHeaders headers = mock(RestInvocation.HttpHeaders.class);
    when(headers.asHttpHeaders()).thenReturn(java.util.Map.of("symbol", "BTCUSDT"));
    when(paramsMap.get(QueryParam.class)).thenReturn(headers);
    when(invocation.getParamsMap()).thenReturn(paramsMap);
    when(invocation.getRequestBody()).thenReturn("");
    when(invocation.getPath()).thenReturn("/api/v3/order");

    String signature = digest.digestParams(invocation);
    assertThat(signature).matches("[0-9a-f]+");
  }

  @Test
  void digestParams_signatureLength() throws Exception {
    BinanceHmacDigest digest = BinanceHmacDigest.createInstance(SECRET_KEY_BASE64);
    RestInvocation invocation = mock(RestInvocation.class);
    when(invocation.getHttpMethod()).thenReturn("GET");

    RestInvocation.HttpParamsMap paramsMap = mock(RestInvocation.HttpParamsMap.class);
    RestInvocation.HttpHeaders headers = mock(RestInvocation.HttpHeaders.class);
    when(headers.asHttpHeaders()).thenReturn(java.util.Map.of("symbol", "BTCUSDT"));
    when(paramsMap.get(QueryParam.class)).thenReturn(headers);
    when(invocation.getParamsMap()).thenReturn(paramsMap);
    when(invocation.getRequestBody()).thenReturn("");
    when(invocation.getPath()).thenReturn("/api/v3/order");

    String signature = digest.digestParams(invocation);
    // SHA-256 produces 32 bytes = 64 hex characters
    assertThat(signature).hasSize(64);
  }

  @Test
  void digestParams_sameParamsSameSignature() throws Exception {
    BinanceHmacDigest digest = BinanceHmacDigest.createInstance(SECRET_KEY_BASE64);

    RestInvocation invocation1 = mock(RestInvocation.class);
    when(invocation1.getHttpMethod()).thenReturn("GET");
    RestInvocation.HttpParamsMap paramsMap1 = mock(RestInvocation.HttpParamsMap.class);
    RestInvocation.HttpHeaders headers1 = mock(RestInvocation.HttpHeaders.class);
    when(headers1.asHttpHeaders()).thenReturn(java.util.Map.of("symbol", "BTCUSDT", "timestamp", "1234567890"));
    when(paramsMap1.get(QueryParam.class)).thenReturn(headers1);
    when(invocation1.getParamsMap()).thenReturn(paramsMap1);
    when(invocation1.getRequestBody()).thenReturn("");
    when(invocation1.getPath()).thenReturn("/api/v3/order");

    RestInvocation invocation2 = mock(RestInvocation.class);
    when(invocation2.getHttpMethod()).thenReturn("GET");
    RestInvocation.HttpParamsMap paramsMap2 = mock(RestInvocation.HttpParamsMap.class);
    RestInvocation.HttpHeaders headers2 = mock(RestInvocation.HttpHeaders.class);
    when(headers2.asHttpHeaders()).thenReturn(java.util.Map.of("symbol", "BTCUSDT", "timestamp", "1234567890"));
    when(paramsMap2.get(QueryParam.class)).thenReturn(headers2);
    when(invocation2.getParamsMap()).thenReturn(paramsMap2);
    when(invocation2.getRequestBody()).thenReturn("");
    when(invocation2.getPath()).thenReturn("/api/v3/order");

    String sig1 = digest.digestParams(invocation1);
    String sig2 = digest.digestParams(invocation2);

    assertThat(sig1).isEqualTo(sig2);
  }

  @Test
  void digestParams_emptyParams() throws Exception {
    BinanceHmacDigest digest = BinanceHmacDigest.createInstance(SECRET_KEY_BASE64);
    RestInvocation invocation = mock(RestInvocation.class);
    when(invocation.getHttpMethod()).thenReturn("GET");

    RestInvocation.HttpParamsMap paramsMap = mock(RestInvocation.HttpParamsMap.class);
    RestInvocation.HttpHeaders headers = mock(RestInvocation.HttpHeaders.class);
    when(headers.asHttpHeaders()).thenReturn(java.util.Map.of());
    when(paramsMap.get(QueryParam.class)).thenReturn(headers);
    when(invocation.getParamsMap()).thenReturn(paramsMap);
    when(invocation.getRequestBody()).thenReturn("");
    when(invocation.getPath()).thenReturn("/api/v3/order");

    String signature = digest.digestParams(invocation);
    assertThat(signature).isNotNull();
    assertThat(signature).isNotEmpty();
    assertThat(signature).hasSize(64);
  }

  @Test
  void digestParams_multipleParams() throws Exception {
    BinanceHmacDigest digest = BinanceHmacDigest.createInstance(SECRET_KEY_BASE64);
    RestInvocation invocation = mock(RestInvocation.class);
    when(invocation.getHttpMethod()).thenReturn("GET");

    RestInvocation.HttpParamsMap paramsMap = mock(RestInvocation.HttpParamsMap.class);
    RestInvocation.HttpHeaders headers = mock(RestInvocation.HttpHeaders.class);
    when(headers.asHttpHeaders()).thenReturn(java.util.Map.of(
        "symbol", "BTCUSDT",
        "timestamp", "1234567890",
        "side", "BUY",
        "type", "LIMIT"
    ));
    when(paramsMap.get(QueryParam.class)).thenReturn(headers);
    when(invocation.getParamsMap()).thenReturn(paramsMap);
    when(invocation.getRequestBody()).thenReturn("");
    when(invocation.getPath()).thenReturn("/api/v3/order");

    String signature = digest.digestParams(invocation);
    assertThat(signature).isNotNull();
    assertThat(signature).isNotEmpty();
    assertThat(signature).hasSize(64);
  }
}
