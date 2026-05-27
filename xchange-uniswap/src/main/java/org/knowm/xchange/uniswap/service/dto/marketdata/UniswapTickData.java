package org.knowm.xchange.uniswap.service.dto.marketdata;

import java.math.BigInteger;

public class UniswapTickData {

  private final int tick;
  private final BigInteger liquidityGross;
  private final BigInteger liquidityNet;
  private final double price0;
  private final double price1;

  public UniswapTickData(
      int tick, BigInteger liquidityGross, BigInteger liquidityNet, double price0, double price1) {
    this.tick = tick;
    this.liquidityGross = liquidityGross;
    this.liquidityNet = liquidityNet;
    this.price0 = price0;
    this.price1 = price1;
  }

  public int getTick() {
    return tick;
  }

  public BigInteger getLiquidityGross() {
    return liquidityGross;
  }

  public BigInteger getLiquidityNet() {
    return liquidityNet;
  }

  public double getPrice0() {
    return price0;
  }

  public double getPrice1() {
    return price1;
  }
}
