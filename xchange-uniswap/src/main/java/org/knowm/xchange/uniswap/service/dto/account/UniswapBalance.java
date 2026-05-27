package org.knowm.xchange.uniswap.service.dto.account;

import java.math.BigDecimal;
import java.math.BigInteger;

public class UniswapBalance {

  private final String tokenAddress;
  private final String symbol;
  private final BigInteger rawBalance;
  private final int decimals;
  private final BigDecimal formattedBalance;

  public UniswapBalance(
      String tokenAddress,
      String symbol,
      BigInteger rawBalance,
      int decimals,
      BigDecimal formattedBalance) {
    this.tokenAddress = tokenAddress;
    this.symbol = symbol;
    this.rawBalance = rawBalance;
    this.decimals = decimals;
    this.formattedBalance = formattedBalance;
  }

  public String getTokenAddress() {
    return tokenAddress;
  }

  public String getSymbol() {
    return symbol;
  }

  public BigInteger getRawBalance() {
    return rawBalance;
  }

  public int getDecimals() {
    return decimals;
  }

  public BigDecimal getFormattedBalance() {
    return formattedBalance;
  }
}
