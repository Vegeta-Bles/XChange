package org.knowm.xchange.uniswap.service.dto.marketdata;

import java.math.BigInteger;

public class UniswapPoolInfo {

  private final String poolAddress;
  private final double price;
  private final int tick;
  private final BigInteger liquidity;
  private final double sqrtPrice;

  public UniswapPoolInfo(
      String poolAddress, double price, int tick, BigInteger liquidity, double sqrtPrice) {
    this.poolAddress = poolAddress;
    this.price = price;
    this.tick = tick;
    this.liquidity = liquidity;
    this.sqrtPrice = sqrtPrice;
  }

  public String getPoolAddress() {
    return poolAddress;
  }

  public double getPrice() {
    return price;
  }

  public int getTick() {
    return tick;
  }

  public BigInteger getLiquidity() {
    return liquidity;
  }

  public double getSqrtPrice() {
    return sqrtPrice;
  }
}
