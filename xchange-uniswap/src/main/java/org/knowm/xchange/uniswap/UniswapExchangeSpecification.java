package org.knowm.xchange.uniswap;

import org.knowm.xchange.ExchangeSpecification;

public class UniswapExchangeSpecification extends ExchangeSpecification {

  public static final String DEFAULT_LOCAL_NODE_URL = "http://localhost:8545";
  public static final String DEFAULT_INFURA_URL = "https://mainnet.infura.io/v3/";
  public static final EthereumNodeType DEFAULT_NODE_TYPE = EthereumNodeType.INFURA;

  private EthereumNodeType ethereumNodeType = DEFAULT_NODE_TYPE;
  private String localNodeUrl = DEFAULT_LOCAL_NODE_URL;
  private String infuraProjectId;

  public UniswapExchangeSpecification() {
    super(UniswapExchange.class);
  }

  @Override
  public String getExchangeClassName() {
    return UniswapExchange.class.getCanonicalName();
  }

  @Override
  public String getExchangeName() {
    return "uniswap";
  }

  @Override
  public String getSslUri() {
    if (ethereumNodeType == EthereumNodeType.LOCAL) {
      return localNodeUrl;
    }
    String baseUrl = DEFAULT_INFURA_URL;
    if (infuraProjectId != null && !infuraProjectId.isEmpty()) {
      return baseUrl + infuraProjectId;
    }
    return baseUrl;
  }

  public EthereumNodeType getEthereumNodeType() {
    return ethereumNodeType;
  }

  public void setEthereumNodeType(EthereumNodeType ethereumNodeType) {
    this.ethereumNodeType = ethereumNodeType;
  }

  public String getLocalNodeUrl() {
    return localNodeUrl;
  }

  public void setLocalNodeUrl(String localNodeUrl) {
    this.localNodeUrl = localNodeUrl;
  }

  public String getInfuraProjectId() {
    return infuraProjectId;
  }

  public void setInfuraProjectId(String infuraProjectId) {
    this.infuraProjectId = infuraProjectId;
  }
}
