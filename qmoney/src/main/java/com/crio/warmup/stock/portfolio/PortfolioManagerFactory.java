
package com.crio.warmup.stock.portfolio;

import com.crio.warmup.stock.quotes.StockQuoteServiceFactory;
import com.crio.warmup.stock.quotes.StockQuotesService;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerFactory {
   
  public static PortfolioManager getPortfolioManager(RestTemplate restTemplate) {
      PortfolioManager obj = new PortfolioManagerImpl(restTemplate);
      
      return obj;
  }
  
   public static PortfolioManager getPortfolioManager(String provider,
     RestTemplate restTemplate) {
      StockQuotesService stockQuotesService = StockQuoteServiceFactory.INSTANCE.getService(provider, restTemplate);
      PortfolioManager obj = new PortfolioManagerImpl(stockQuotesService);
      return obj;
   }

}
