
package com.crio.warmup.stock.quotes;

import org.springframework.web.client.RestTemplate;

public enum StockQuoteServiceFactory {

  // Note: (Recommended reading)
  // Pros and cons of implementing Singleton via enum.
  // https://softwareengineering.stackexchange.com/q/179386/253205

  INSTANCE;

  public StockQuotesService getService(String provider,  RestTemplate restTemplate) {
      // go to Tingo Service
      if (provider == null) {
        return new AlphavantageService(restTemplate);
      }
      else if ("tiingo".equals(provider.toLowerCase())) {
            return new TiingoService(restTemplate);
      }
      else {
        return new AlphavantageService(restTemplate);
      }
    
  }

}
