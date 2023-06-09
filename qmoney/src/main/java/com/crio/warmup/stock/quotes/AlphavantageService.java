
package com.crio.warmup.stock.quotes;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;
import com.crio.warmup.stock.dto.AlphavantageCandle;
import com.crio.warmup.stock.dto.AlphavantageDailyResponse;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class AlphavantageService implements StockQuotesService {

  private RestTemplate restTemplate;

  public static final String TOKEN = "PHZKOUTN7XHSAK3I";
  public static final String FUNCTION = "TIME_SERIES_DAILY_ADJUSTED";

  protected AlphavantageService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }
 
  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException, StockQuoteServiceException {
        String url = buildUri(symbol, from, to);
        List<Candle> stocks = new ArrayList<>();

        try {
        String apiResponse = restTemplate.getForObject(url, String.class);
        //System.out.println(apiResponse);

        ObjectMapper objectMapper = getObjectMapper();

        Map<LocalDate, AlphavantageCandle> dailyResponse = objectMapper.readValue(apiResponse,
                                                               AlphavantageDailyResponse.class).getCandles();
        
        for (LocalDate date = from; !date.isAfter(to) ; date = date.plusDays(1)) {
            AlphavantageCandle candle = dailyResponse.get(date);

            if (candle != null) {
              candle.setDate(date);
              stocks.add(candle);
            }
        }
       } catch(NullPointerException e) {
            throw new StockQuoteServiceException("Alphavantage returned Invalid Response ", e);
       }

        return stocks;
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
      String uriTemplate = "https://www.alphavantage.co/query?function=" + FUNCTION + "&symbol=" + symbol + "&apikey=" +TOKEN;
      return uriTemplate;
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }
  
}

