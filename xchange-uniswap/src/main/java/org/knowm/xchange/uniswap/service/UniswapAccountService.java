package org.knowm.xchange.uniswap.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.Wallet;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.service.BaseExchangeService;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;
import org.knowm.xchange.service.trade.params.WithdrawFundsParams;
import org.knowm.xchange.uniswap.UniswapExchange;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

public class UniswapAccountService extends BaseExchangeService<UniswapExchange>
    implements AccountService {

  private static final String WETH_ADDRESS =
      "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2";

  private Web3j getWeb3j() {
    return Web3j.build(new HttpService(exchange.getExchangeSpecification().getSslUri()));
  }

  private String getWalletAddress() {
    String address = exchange.getExchangeSpecification().getApiKey();
    if (address == null || address.isEmpty()) {
      throw new IOException("Wallet address not configured in exchange specification");
    }
    return address;
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

  private BigDecimal getEthBalance(Web3j web3j, String address) throws IOException {
    EthGetBalance ethGetBalance =
        web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
    if (ethGetBalance == null) {
      return BigDecimal.ZERO;
    }
    return Convert.fromWei(
        new BigDecimal(ethGetBalance.getBalance().toString()), Convert.Unit.ETHER);
  }

  private BigDecimal getTokenBalance(Web3j web3j, String address, String tokenAddress)
      throws IOException {
    try {
      byte[] methodId = hexStringToByteArray("70a08231");
      byte[] addressBytes = hexStringToByteArray(address.substring(2));
      byte[] addressPadding = new byte[12];
      byte[] data = new byte[methodId.length + addressPadding.length + addressBytes.length];
      System.arraycopy(methodId, 0, data, 0, methodId.length);
      System.arraycopy(addressPadding, 0, data, methodId.length, addressPadding.length);
      System.arraycopy(addressBytes, 0, data, methodId.length + addressPadding.length, addressBytes.length);

      org.web3j.protocol.core.methods.response.EthCall ethCall =
          web3j.ethCall(
                  org.web3j.tx.Contract.createCall(data, tokenAddress),
                  DefaultBlockParameterName.LATEST)
              .send();

      if (ethCall != null && ethCall.getValue() != null && ethCall.getValue().length() > 66) {
        String balanceHex = ethCall.getValue().substring(130);
        BigInteger balance = new BigInteger(balanceHex, 16);
        return new BigDecimal(balance, 0);
      }
    } catch (Exception e) {
      return BigDecimal.ZERO;
    }
    return BigDecimal.ZERO;
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
  public AccountInfo getAccountInfo() throws IOException {
    String walletAddress = getWalletAddress();
    Web3j web3j = getWeb3j();

    List<Balance> balances = new ArrayList<>();

    BigDecimal ethBalance = getEthBalance(web3j, walletAddress);
    balances.add(new Balance(Currency.getInstance("ETH"), null, ethBalance, BigDecimal.ZERO));

    Map<String, String> tokens = getTokenAddresses();
    for (Map.Entry<String, String> entry : tokens.entrySet()) {
      String symbol = entry.getKey();
      String tokenAddress = entry.getValue();
      if (symbol.equals("ETH")) {
        continue;
      }

      try {
        BigDecimal tokenBalance = getTokenBalance(web3j, walletAddress, tokenAddress);
        if (tokenBalance.compareTo(BigDecimal.ZERO) > 0) {
          balances.add(
              new Balance(Currency.getInstance(symbol), null, tokenBalance, BigDecimal.ZERO));
        }
      } catch (Exception e) {
        // Skip tokens that fail to query
      }
    }

    return new AccountInfo(
        Wallet.Builder.from(balances).id(walletAddress.substring(0, Math.min(8, walletAddress.length()))).build());
  }

  @Override
  public String requestDepositAddress(Currency currency, String... args) {
    return getWalletAddress();
  }

  @Override
  public List<org.knowm.xchange.dto.account.FundingRecord> getFundingHistory(
      TradeHistoryParams params) {
    throw new NotAvailableFromExchangeException(
        "Funding history not available. Use Ethereum block explorer for transaction history.");
  }

  @Override
  public TradeHistoryParams createFundingHistoryParams() {
    throw new NotAvailableFromExchangeException(
        "Funding history params not available for Uniswap");
  }

  @Override
  public String withdrawFunds(WithdrawFundsParams withdrawFundsParams)
      throws IOException {
    throw new NotAvailableFromExchangeException(
        "Withdrawals must be done directly via Ethereum wallet, not through this adapter");
  }

  @Override
  public String withdrawFunds(Currency currency, BigDecimal amount, String address)
      throws IOException {
    throw new NotAvailableFromExchangeException(
        "Withdrawals must be done directly via Ethereum wallet, not through this adapter");
  }
}
