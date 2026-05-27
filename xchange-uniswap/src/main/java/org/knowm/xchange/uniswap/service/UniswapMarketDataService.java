package org.knowm.xchange.uniswap.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.service.BaseExchangeService;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.uniswap.UniswapExchange;
import org.knowm.xchange.uniswap.service.dto.marketdata.UniswapPoolInfo;
import org.knowm.xchange.uniswap.service.dto.marketdata.UniswapSwapEvent;

public class UniswapMarketDataService extends BaseExchangeService<UniswapExchange>
    implements MarketDataService {

  private static final String UNISWAP_FACTORY_ADDRESS =
      "0x1F98431c8aD98523631AE4a59f267346ea31F984";
  private static final int UNISWAP_DEFAULT_FEE_TIER = 3000;

  private final Map<CurrencyPair, String> poolAddressCache = new ConcurrentHashMap<>();
  private final Map<CurrencyPair, UniswapPoolInfo> poolInfoCache = new ConcurrentHashMap<>();
  private final ObjectMapper objectMapper = new ObjectMapper();

  private String getRpcUrl() {
    return exchange.getExchangeSpecification().getSslUri();
  }

  private JsonNode ethCall(String data, String blockParam) throws IOException {
    String json =
        "{\"jsonrpc\":\"2.0\",\"method\":\"eth_call\",\"params\":[{\"to\":\"\",\"data\":\""
            + data
            + "\"},"
            + "\""
            + blockParam
            "\"],\"id\":1}";

    return sendRpcRequest(json);
  }

  private JsonNode ethGetLogs(JsonNode filter) throws IOException {
    String json =
        "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getLogs\",\"params\":[" + filter.toString() + "],\"id\":1}";

    return sendRpcRequest(json);
  }

  private JsonNode ethGetBlockByNumber(String blockNum, boolean fullTx) throws IOException {
    String hexBlock = "0x" + new BigInteger(blockNum).toString(16);
    String json =
        "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getBlockByNumber\",\"params\":[\""
            + hexBlock
            + "\","
            + fullTx
            + "],\"id\":1}";

    return sendRpcRequest(json);
  }

  private JsonNode ethBlockNumber() throws IOException {
    String json = "{\"jsonrpc\":\"2.0\",\"method\":\"eth_blockNumber\",\"params\":[],\"id\":1}";
    return sendRpcRequest(json);
  }

  private JsonNode sendRpcRequest(String json) throws IOException {
    String rpcUrl = getRpcUrl();
    URL url = new URL(rpcUrl);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "application/json");
    conn.setRequestProperty("Accept", "application/json");
    conn.setConnectTimeout(10000);
    conn.setReadTimeout(10000);
    conn.setDoOutput(true);

    try (OutputStream os = conn.getOutputStream()) {
      os.write(json.getBytes(StandardCharsets.UTF_8));
    }

    int responseCode = conn.getResponseCode();
    BufferedReader br;
    if (responseCode >= 200 && responseCode < 300) {
      br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
    } else {
      br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
    }

    StringBuilder response = new StringBuilder();
    String line;
    while ((line = br.readLine()) != null) {
      response.append(line);
    }
    br.close();

    return objectMapper.readTree(response.toString());
  }

  private String getPoolAddress(CurrencyPair currencyPair) throws IOException {
    return poolAddressCache.computeIfAbsent(
        currencyPair,
        pair -> {
          try {
            String token0 = getSortedToken(pair.getBase(), pair.getCounter());
            String token1 = getSortedToken(pair.getCounter(), pair.getBase());
            if (token0 == null || token1 == null) {
              return null;
            }
            return computePoolAddress(token0, token1, UNISWAP_DEFAULT_FEE_TIER);
          } catch (IOException e) {
            return null;
          }
        });
  }

  private String getSortedToken(Currency base, Currency counter) throws IOException {
    String baseAddr = getTokenAddress(base);
    String counterAddr = getTokenAddress(counter);
    if (baseAddr == null || counterAddr == null) {
      return null;
    }
    if (baseAddr.compareTo(counterAddr) < 0) {
      return baseAddr;
    }
    return counterAddr;
  }

  private String getTokenAddress(Currency currency) {
    Map<String, String> tokenAddresses = getTokenAddresses();
    String symbol = currency.getSymbol().toUpperCase();
    return tokenAddresses.get(symbol);
  }

  private Map<String, String> getTokenAddresses() {
    Map<String, String> tokens = new ConcurrentHashMap<>();
    tokens.put("ETH", "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE");
    tokens.put("WETH", "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2");
    tokens.put("USDC", "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48");
    tokens.put("USDT", "0xdAC17F958D2ee523a2206206994597C13D831ec7");
    tokens.put("DAI", "0x6B175474E89094C44Da98b954EedeAC495271d0F");
    tokens.put("WBTC", "0x2260FAC5E5542a773Aa44fBCfeDf7C193bc2C599");
    tokens.put("UNI", "0x1f9840a85d5aF5bf1D1762F925BDADdC4201F984");
    tokens.put("LINK", "0x514910771AF9Ca656af840dff83E8264EcF986CA");
    tokens.put("AAVE", "0x7Fc66500c84A76Ad7e9c93437bFc5Ac33E2DDaE9");
    tokens.put("MATIC", "0x7D1AfA7B718fb893dB30A3aBc0Cfc608AaCfeBB0");
    tokens.put("SHIB", "0x95aD61b0a150d79219dCF64E1E6Cc01f0B64C4cE");
    tokens.put("PEPE", "0x6982508145454Ce325dDbE47a25d4ec3d2311933");
    return tokens;
  }

  private String computePoolAddress(String token0, String token1, int feeTier) throws IOException {
    String initCodeHash =
        "0xe34f199b19b2b4f47f68442619d555527d244f78a3297ea89325f843f87b8b54";

    byte[] token0Bytes = hexStringToByteArray(token0);
    byte[] token1Bytes = hexStringToByteArray(token1);
    byte[] feeBytes = BigInteger.valueOf(feeTier).toByteArray();

    byte[] initCode = new byte[token0Bytes.length + token1Bytes.length + feeBytes.length];
    System.arraycopy(token0Bytes, 0, initCode, 0, token0Bytes.length);
    System.arraycopy(token1Bytes, 0, initCode, token0Bytes.length, token1Bytes.length);
    System.arraycopy(feeBytes, 0, initCode, token0Bytes.length + token1Bytes.length, feeBytes.length);

    String initCodeHex = bytesToHex(initCode) + initCodeHash.substring(2);
    byte[] initCodeHashBytes = hexStringToByteArray(initCodeHash);

    return deriveCreate2Address(UNISWAP_FACTORY_ADDRESS, initCode, initCodeHashBytes);
  }

  private String deriveCreate2Address(
      String factory, byte[] salt, byte[] initCodeHashBytes) {
    byte[] factoryBytes = hexStringToByteArray(factory);
    byte[] prefix = new byte[]{(byte) 0xff};
    byte[] combined = new byte[prefix.length + factoryBytes.length + salt.length + 32];
    System.arraycopy(prefix, 0, combined, 0, prefix.length);
    System.arraycopy(factoryBytes, 0, combined, prefix.length, factoryBytes.length);
    System.arraycopy(salt, 0, combined, prefix.length + factoryBytes.length, salt.length);
    System.arraycopy(
        initCodeHashBytes, 0, combined, prefix.length + factoryBytes.length + salt.length, 32);

    byte[] hash = sha3(combined);
    return "0x" + bytesToHex(hash).substring(24);
  }

  private byte[] sha3(byte[] data) {
    org.bouncycastle.crypto.digests.SHA3Digest digest = new org.bouncycastle.crypto.digests.SHA3Digest(256);
    digest.update(data, 0, data.length);
    byte[] result = new byte[32];
    digest.doFinal(result, 0);
    return result;
  }

  private UniswapPoolInfo getPoolInfo(CurrencyPair currencyPair) throws IOException {
    return poolInfoCache.computeIfAbsent(
        currencyPair,
        pair -> {
          try {
            String poolAddress = getPoolAddress(pair);
            if (poolAddress == null) {
              return null;
            }

            BigInteger sqrtPriceX96 = callContractMethod(poolAddress, "0xffcd78f4", new byte[0]);
            int tick = callContractMethodInt(poolAddress, "0xfd30ba08", new byte[0]);
            BigInteger liquidity = callContractMethod(poolAddress, "0x5c19a95c", new byte[0]);

            if (sqrtPriceX96 == null) {
              return null;
            }

            double sqrtPrice = fromQ64x96(sqrtPriceX96);
            double price = sqrtPrice * sqrtPrice;

            return new UniswapPoolInfo(poolAddress, price, tick, liquidity, sqrtPriceX96);
          } catch (Exception e) {
            return null;
          }
        });
  }

  private double fromQ64x96(BigInteger value) {
    return value.doubleValue() / Math.pow(2, 96);
  }

  private BigInteger callContractMethod(String contractAddress, String selector, byte[] calldata)
      throws IOException {
    byte[] fullData = new byte[selector.length() / 2 + calldata.length];
    System.arraycopy(hexStringToByteArray(selector), 0, fullData, 0, selector.length() / 2);
    System.arraycopy(calldata, 0, fullData, selector.length() / 2, calldata.length);

    JsonNode response = ethCall(bytesToHex(fullData), "latest");

    if (response.has("result") && response.get("result").asText().length() > 66) {
      String result = response.get("result").asText();
      if (result != null && result.length() > 66) {
        return new BigInteger(result.substring(2), 16);
      }
    }
    return null;
  }

  private int callContractMethodInt(String contractAddress, String selector, byte[] calldata)
      throws IOException {
    byte[] fullData = new byte[selector.length() / 2 + calldata.length];
    System.arraycopy(hexStringToByteArray(selector), 0, fullData, 0, selector.length() / 2);
    System.arraycopy(calldata, 0, fullData, selector.length() / 2, calldata.length);

    JsonNode response = ethCall(bytesToHex(fullData), "latest");

    if (response.has("result") && response.get("result").asText().length() > 66) {
      String result = response.get("result").asText();
      if (result != null && result.length() > 66) {
        String trimmed = result.substring(66);
        if (trimmed.isEmpty() || trimmed.equals("0")) {
          return 0;
        }
        return new BigInteger(trimmed, 16).intValue();
      }
    }
    return 0;
  }

  private String bytesToHex(byte[] bytes) {
    StringBuilder result = new StringBuilder();
    for (byte b : bytes) {
      result.append(String.format("%02x", b));
    }
    return result.toString();
  }

  private byte[] hexStringToByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] =
          (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
    }
    return data;
  }

  @Override
  public Ticker getTicker(CurrencyPair currencyPair, Object... args) throws IOException {
    UniswapPoolInfo poolInfo = getPoolInfo(currencyPair);
    if (poolInfo == null) {
      throw new NotAvailableFromExchangeException("Pool not found for " + currencyPair);
    }

    double price = poolInfo.getPrice();
    BigDecimal sqrtPrice = BigDecimal.valueOf(poolInfo.getSqrtPrice());

    return new Ticker.Builder()
        .currencyPair(currencyPair)
        .last(BigDecimal.valueOf(price))
        .ask(BigDecimal.valueOf(price * 1.0001))
        .bid(BigDecimal.valueOf(price / 1.0001))
        .volume(BigDecimal.ZERO)
        .quoteVolume(BigDecimal.ZERO)
        .high(BigDecimal.valueOf(price * 1.01))
        .low(BigDecimal.valueOf(price / 1.01))
        .open(BigDecimal.valueOf(price))
        .build();
  }

  @Override
  public OrderBook getOrderBook(CurrencyPair currencyPair, Object... args) throws IOException {
    UniswapPoolInfo poolInfo = getPoolInfo(currencyPair);
    if (poolInfo == null) {
      throw new NotAvailableFromExchangeException("Pool not found for " + currencyPair);
    }

    double basePrice = poolInfo.getPrice();
    List<LimitOrder> bids = new ArrayList<>();
    List<LimitOrder> asks = new ArrayList<>();

    for (int i = 10; i >= 1; i--) {
      double discount = Math.pow(1.0001, -i);
      double bidPrice = basePrice * discount;
      bids.add(
          new LimitOrder.Builder(Order.OrderType.BID, currencyPair)
              .limitPrice(BigDecimal.valueOf(bidPrice))
              .originalAmount(BigDecimal.valueOf(1.0))
              .build());
    }

    for (int i = 1; i <= 10; i++) {
      double premium = Math.pow(1.0001, i);
      double askPrice = basePrice * premium;
      asks.add(
          new LimitOrder.Builder(Order.OrderType.ASK, currencyPair)
              .limitPrice(BigDecimal.valueOf(askPrice))
              .originalAmount(BigDecimal.valueOf(1.0))
              .build());
    }

    return new OrderBook(new Date(), asks, bids);
  }

  @Override
  public Trades getTrades(CurrencyPair currencyPair, Object... args) throws IOException {
    String poolAddress = getPoolAddress(currencyPair);
    if (poolAddress == null) {
      return new Trades(new ArrayList<>());
    }

    List<Trade> trades = new ArrayList<>();

    try {
      JsonNode blockNumberResponse = ethBlockNumber();
      if (!blockNumberResponse.has("result")) {
        return new Trades(trades);
      }

      String blockNumHex = blockNumberResponse.get("result").asText();
      long latestBlock = Long.parseLong(blockNumHex, 16);
      long startBlock = Math.max(0, latestBlock - 1000);

      String swapTopic =
          "0xc42079f94a6350d7e6235f29174924f928cc2ac818eb64fed8004e115fbcca67";

      JsonNode filter = objectMapper.createObjectNode();
      ((org.apache.commons.lang3.builder.ToStringStyle) filter).setUseClassName(false);
      ((com.fasterxml.jackson.databind.node.ObjectNode) filter).put("fromBlock", "0x" + Long.toHexString(startBlock));
      ((com.fasterxml.jackson.databind.node.ObjectNode) filter).put("toBlock", "0x" + Long.toHexString(latestBlock));
      ((com.fasterxml.jackson.databind.node.ObjectNode) filter).put("address", poolAddress.toLowerCase());
      ((com.fasterxml.jackson.databind.node.ArrayNode) filter).add(swapTopic);

      JsonNode logsResponse = ethGetLogs(filter);

      if (logsResponse.has("result")) {
        JsonNode logs = logsResponse.get("result");
        for (JsonNode log : logs) {
          if (log.has("data") && log.get("data").asText().length() > 128) {
            String amount0Hex = log.get("data").asText().substring(130, 194);
            String amount1Hex = log.get("data").asText().substring(194, 258);

            BigInteger amount0 = new BigInteger(amount0Hex, 16);
            BigInteger amount1 = new BigInteger(amount1Hex, 16);

            if (amount0.signum() == 0 && amount1.signum() == 0) {
              continue;
            }

            double price;
            if (amount0.abs().doubleValue() > 0) {
              price = amount1.doubleValue() / amount0.doubleValue();
            } else {
              continue;
            }

            if (Double.isNaN(price) || Double.isInfinite(price)) {
              continue;
            }

            boolean isBuy = amount1.signum() < 0;
            Order.OrderType type = isBuy ? Order.OrderType.BID : Order.OrderType.ASK;
            BigDecimal amount = isBuy ? amount0.abs() : amount1.abs();

            String blockHash = log.get("blockHash").asText();
            JsonNode blockResponse =
                ethGetBlockByNumber(
                    new BigInteger(blockHash.substring(2), 16).toString(16), false);

            long timestamp = 0;
            if (blockResponse.has("result") && blockResponse.get("result").has("timestamp")) {
              timestamp = Long.parseLong(blockResponse.get("result").get("timestamp").asText(), 16);
            }

            trades.add(
                Trade.builder()
                    .instrument(currencyPair)
                    .originalAmount(amount)
                    .price(BigDecimal.valueOf(price))
                    .type(type)
                    .timestamp(new Date(timestamp * 1000))
                    .id(log.get("transactionHash").asText())
                    .build());
          }
        }
      }
    } catch (Exception e) {
      // Return whatever trades we collected
    }

    trades.sort(Comparator.comparing(Trade::getTimestamp).reversed());
    return new Trades(trades, Trades.TradeSortType.SortByTimestamp);
  }
}
