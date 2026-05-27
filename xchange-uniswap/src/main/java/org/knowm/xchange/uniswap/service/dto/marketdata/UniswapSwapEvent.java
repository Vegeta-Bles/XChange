package org.knowm.xchange.uniswap.service.dto.marketdata;

import java.math.BigInteger;

public class UniswapSwapEvent {

  private final String transactionHash;
  private final BigInteger amount0;
  private final BigInteger amount1;
  private final long blockTimestamp;

  public UniswapSwapEvent(
      String transactionHash,
      BigInteger amount0,
      BigInteger amount1,
      long blockTimestamp) {
    this.transactionHash = transactionHash;
    this.amount0 = amount0;
    this.amount1 = amount1;
    this.blockTimestamp = blockTimestamp;
  }

  public String getTransactionHash() {
    return transactionHash;
  }

  public BigInteger getAmount0() {
    return amount0;
  }

  public BigInteger getAmount1() {
    return amount1;
  }

  public long getBlockTimestamp() {
    return blockTimestamp;
  }
}
