package org.knowm.xchange.uniswap.service.dto.trade;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;

public class UniswapTradeHistoryParams implements TradeHistoryParams {

  private final String walletAddress;
  private CurrencyPair currencyPair;

  public UniswapTradeHistoryParams(String walletAddress) {
    this.walletAddress = walletAddress;
  }

  public String getWalletAddress() {
    return walletAddress;
  }

  public CurrencyPair getCurrencyPair() {
    return currencyPair;
  }

  public void setCurrencyPair(CurrencyPair currencyPair) {
    this.currencyPair = currencyPair;
  }
}
