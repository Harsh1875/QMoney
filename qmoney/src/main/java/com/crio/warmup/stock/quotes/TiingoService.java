
package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.client.RestTemplate;

public class TiingoService implements StockQuotesService {

  private RestTemplate restTemplate;

  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }


  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Implement getStockQuote method below that was also declared in the interface.
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to) throws JsonProcessingException {
        List<Candle> result = new ArrayList<>();
        if (from.compareTo(to) >= 0) {
            throw new RuntimeException();
        }
        String uri = buildUri(symbol, from, to);

        String stock = restTemplate.getForObject(uri, String.class);
        ObjectMapper objectMapper = getObjectMapper();

        TiingoCandle[] tempCandles = objectMapper.readValue(stock, TiingoCandle[].class);

        if (tempCandles != null) {
            result = Arrays.asList(tempCandles);
        } else {
            result = Arrays.asList(new TiingoCandle[0]);
        }

        return result;
  }
  // Note:
  // 1. You can move the code from PortfolioManagerImpl#getStockQuote inside newly created method.
  // 2. Run the tests using command below and make sure it passes.
  //    ./gradlew test --tests TiingoServiceTest

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String uriTemplate = "https:api.tiingo.com/tiingo/daily/" +symbol + "/prices?"
         + "startDate=" + startDate + "&endDate=" + endDate + "&token=1135a69fa31f4622bd5b50f6854acbdfa55ee1ec";
     return uriTemplate;
  }
  //CHECKSTYLE:OFF

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }
  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Write a method to create appropriate url to call the Tiingo API.

}
