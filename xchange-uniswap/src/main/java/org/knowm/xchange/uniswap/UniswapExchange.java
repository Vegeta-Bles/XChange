package org.knowm.xchange.uniswap;

import org.knowm.xchange.BaseExchange;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.uniswap.service.UniswapAccountService;
import org.knowm.xchange.uniswap.service.UniswapMarketDataService;
import org.knowm.xchange.uniswap.service.UniswapTradeService;

public class UniswapExchange extends BaseExchange {

  @Override
  protected void initServices() {
    this.marketDataService = new UniswapMarketDataService(this);
    this.tradeService = new UniswapTradeService(this);
    this.accountService = new UniswapAccountService(this);
  }

  @Override
  public ExchangeSpecification getDefaultExchangeSpecification() {
    UniswapExchangeSpecification exchangeSpecification = new UniswapExchangeSpecification();
    exchangeSpecification.setExchangeName("Uniswap");
    exchangeSpecification.setExchangeDescription("Uniswap V3 DEX integration via Ethereum JSON-RPC");
    exchangeSpecification.setShouldLoadRemoteMetaData(false);
    return exchangeSpecification;
  }
}
