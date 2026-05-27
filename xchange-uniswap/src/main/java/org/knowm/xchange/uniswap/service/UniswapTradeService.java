package org.knowm.xchange.uniswap.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.dto.trade.StopOrder;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.dto.trade.UserTrades;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.service.BaseExchangeService;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.CancelOrderParams;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParams;
import org.knowm.xchange.uniswap.UniswapExchange;
import org.knowm.xchange.uniswap.service.dto.trade.UniswapTradeHistoryParams;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.utils.Numeric;

public class UniswapTradeService extends BaseExchangeService<UniswapExchange>
    implements TradeService {

  private static final String UNISWAP_V3_ROUTER_ADDRESS =
      "0xE592427A0AEce92De3Edee1F18E0157C05861564";
  private static final String WETH_ADDRESS =
      "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2";

  private final ObjectMapper objectMapper = new ObjectMapper();

  private String getRpcUrl() {
    String apiUrl = exchange.getExchangeSpecification().getSslUri();
    String apiKey = exchange.getExchangeSpecification().getApiKey();
    String fullUrl = apiUrl;
    if (apiKey != null && !apiKey.isEmpty()) {
      fullUrl = apiUrl + apiKey;
    }
    return fullUrl;
  }

  private JsonNode sendRpcRequest(String json) throws IOException {
    java.net.URL url = new java.net.URL(getRpcUrl());
    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
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

  private JsonNode ethGetTransactionCount(String address, String blockParam) throws IOException {
    String paddedAddress = Numeric.prependHexZeroes(address.substring(2), 64);
    String json =
        "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getTransactionCount\",\"params\":[\""
            + address
            + "\",\""
            + blockParam
            "\"],\"id\":1}";
    return sendRpcRequest(json);
  }

  private JsonNode ethGetGasPrice() throws IOException {
    String json = "{\"jsonrpc\":\"2.0\",\"method\":\"eth_gasPrice\",\"params\":[],\"id\":1}";
    return sendRpcRequest(json);
  }

  private JsonNode ethSendRawTransaction(String signedTx) throws IOException {
    String json =
        "{\"jsonrpc\":\"2.0\",\"method\":\"eth_sendRawTransaction\",\"params\":[\""
            + signedTx
            + "\"],\"id\":1}";
    return sendRpcRequest(json);
  }

  @Override
  public OpenOrders getOpenOrders() throws IOException {
    return new OpenOrders(Collections.emptyList());
  }

  @Override
  public OpenOrders getOpenOrders(OpenOrdersParams params) throws IOException {
    return getOpenOrders();
  }

  @Override
  public boolean cancelOrder(String orderId) throws IOException {
    throw new NotAvailableFromExchangeException(
        "Uniswap V3 does not support order cancellation. Use exact amount trades instead.");
  }

  @Override
  public boolean cancelOrder(CancelOrderParams cancelOrderParams) throws IOException {
    throw new NotAvailableFromExchangeException(
        "Uniswap V3 does not support order cancellation. Use exact amount trades instead.");
  }

  @Override
  public UserTrades getTradeHistory(TradeHistoryParams params) throws IOException {
    String address = exchange.getExchangeSpecification().getApiKey();
    if (address == null || address.isEmpty()) {
      throw new IOException("Wallet address not configured");
    }

    List<UserTrade> userTrades = new java.util.ArrayList<>();

    try {
      CurrencyPair pair = null;
      if (params instanceof UniswapTradeHistoryParams) {
        pair = ((UniswapTradeHistoryParams) params).getCurrencyPair();
      }

      return new UserTrades(userTrades, Trades.TradeSortType.SortByTimestamp);
    } catch (Exception e) {
      return new UserTrades(Collections.emptyList(), Trades.TradeSortType.SortByTimestamp);
    }
  }

  @Override
  public OpenOrdersParams createOpenOrdersParams() {
    throw new NotAvailableFromExchangeException("Uniswap V3 does not use limit orders");
  }

  @Override
  public void verifyOrder(LimitOrder limitOrder) {
    throw new NotAvailableFromExchangeException(
        "Uniswap V3 does not support limit orders. Use market orders with exact output/input amounts.");
  }

  @Override
  public void verifyOrder(MarketOrder marketOrder) {
    if (marketOrder.getOriginalAmount() == null) {
      throw new IllegalArgumentException("Market order requires amount");
    }
  }

  @Override
  public TradeHistoryParams createTradeHistoryParams() {
    return new UniswapTradeHistoryParams(
        exchange.getExchangeSpecification().getApiKey());
  }

  @Override
  public String placeMarketOrder(MarketOrder marketOrder) throws IOException {
    String walletAddress = exchange.getExchangeSpecification().getApiKey();
    String privateKey = exchange.getExchangeSpecification().getSecretKey();

    if (walletAddress == null || walletAddress.isEmpty()) {
      throw new IOException("Wallet address not configured in exchange specification");
    }
    if (privateKey == null || privateKey.isEmpty()) {
      throw new IOException("Private key not configured in exchange specification");
    }

    CurrencyPair pair = marketOrder.getCurrencyPair();
    BigDecimal amount = marketOrder.getOriginalAmount();

    String tokenIn;
    String tokenOut;

    if (pair.getBase().getSymbol().equals("ETH") || pair.getBase().getSymbol().equals("WETH")) {
      tokenIn = WETH_ADDRESS;
      tokenOut = getTokenAddress(pair.getCounter());
    } else {
      tokenIn = getTokenAddress(pair.getBase());
      tokenOut = WETH_ADDRESS;
    }

    BigInteger amountIn =
        toWei(amount, getTokenDecimals(pair.getBase()));

    Credentials credentials = Credentials.create(privateKey);

    JsonNode nonceResponse =
        ethGetTransactionCount(walletAddress.toLowerCase(), "latest");
    long nonce = Long.parseLong(nonceResponse.get("result").asText(), 16);

    JsonNode gasPriceResponse = ethGetGasPrice();
    BigInteger gasPrice = new BigInteger(gasPriceResponse.get("result").asText(), 16);

    byte[] methodId = hexStringToByteArray("3593564c");
    byte[] amountInBytes = padTo32(amountIn.toByteArray());
    byte[] recipientBytes = hexStringToByteArray(walletAddress.substring(2));
    byte[] recipientPadding = new byte[12];
    byte[] data = new byte[methodId.length + amountInBytes.length + recipientPadding.length + recipientBytes.length];
    System.arraycopy(methodId, 0, data, 0, methodId.length);
    System.arraycopy(amountInBytes, 0, data, methodId.length, amountInBytes.length);
    System.arraycopy(recipientPadding, 0, data, methodId.length + amountInBytes.length, recipientPadding.length);
    System.arraycopy(recipientBytes, 0, data, methodId.length + amountInBytes.length + recipientPadding.length, recipientBytes.length);

    RawTransaction rawTransaction =
        RawTransaction.createTransaction(
            nonce, gasPrice, BigInteger.valueOf(300000), UNISWAP_V3_ROUTER_ADDRESS, BigInteger.ZERO, data);

    byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
    String hexData = Numeric.toHexString(signedMessage);

    JsonNode sendResponse = ethSendRawTransaction(hexData);

    if (sendResponse.has("error")) {
      throw new IOException(
          "Transaction failed: " + sendResponse.get("error").get("message").asText());
    }

    return sendResponse.get("result").asText();
  }

  @Override
  public String placeLimitOrder(LimitOrder limitOrder) {
    throw new NotAvailableFromExchangeException(
        "Uniswap V3 does not support limit orders. Use market orders instead.");
  }

  @Override
  public String placeStopOrder(StopOrder stopOrder) {
    throw new NotAvailableFromExchangeException(
        "Uniswap V3 does not support stop orders.");
  }

  private String getTokenAddress(Currency currency) {
    Map<String, String> tokens = getTokenAddresses();
    String symbol = currency.getSymbol().toUpperCase();
    String addr = tokens.get(symbol);
    if (addr == null) {
      throw new IllegalArgumentException("Token not supported: " + currency.getSymbol());
    }
    return addr;
  }

  private Map<String, String> getTokenAddresses() {
    Map<String, String> tokens = new HashMap<>();
    tokens.put("ETH", "0x0000000000000000000000000000000000000000");
    tokens.put("WETH", "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2");
    tokens.put("USDC", "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48");
    tokens.put("USDT", "0xdAC17F958D2ee523a2206206994597C13D831ec7");
    tokens.put("DAI", "0x6B175474E89094C44Da98b954EedeAC495271d0F");
    tokens.put("WBTC", "0x2260FAC5E5542a773Aa44fBCfeDf7C193bc2C599");
    tokens.put("UNI", "0x1f9840a85d5aF5bf1D1762F925BDADdC4201F984");
    tokens.put("LINK", "0x514910771AF9Ca656af840dff83E8264EcF986CA");
    return tokens;
  }

  private int getTokenDecimals(Currency currency) {
    String symbol = currency.getSymbol().toUpperCase();
    if (symbol.equals("ETH") || symbol.equals("WETH")) {
      return 18;
    }
    if (symbol.equals("USDC")) {
      return 6;
    }
    if (symbol.equals("USDT") || symbol.equals("DAI") || symbol.equals("WBTC") || symbol.equals("UNI") || symbol.equals("LINK")) {
      return 18;
    }
    return 18;
  }

  private BigInteger toWei(BigDecimal amount, int decimals) {
    return amount.multiply(BigDecimal.TEN.pow(decimals)).toBigInteger();
  }

  private byte[] padTo32(byte[] input) {
    if (input.length == 32) {
      return input;
    }
    byte[] padded = new byte[32];
    int offset = 32 - input.length;
    System.arraycopy(input, 0, padded, offset, input.length);
    return padded;
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
}
