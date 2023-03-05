
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {

  private RestTemplate restTemplate;

  private StockQuotesService stockQuotesService;

  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  protected PortfolioManagerImpl(StockQuotesService stockQuotesService) {
    this.stockQuotesService = stockQuotesService;
  }

  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturnParallel(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate, int numThreads) throws InterruptedException,StockQuoteServiceException, JsonProcessingException {
        
        List<AnnualizedReturn> annualizedReturns = new ArrayList<>();
        List<Future<AnnualizedReturn>> futureReturnList = new ArrayList<>();

        final ExecutorService pool = Executors.newFixedThreadPool(numThreads);

        for (int i=0; i < portfolioTrades.size(); i++) {
            PortfolioTrade trade = portfolioTrades.get(i);
            Callable<AnnualizedReturn> callableTask = () -> {
                return getAnnualizedReturn(trade, endDate);
            };
            Future<AnnualizedReturn> futureReturn = pool.submit(callableTask);  
            futureReturnList.add(futureReturn);
        }

        for (int i=0; i < portfolioTrades.size(); i++) {
          Future<AnnualizedReturn> futureReturn = futureReturnList.get(i);
          try {
              AnnualizedReturn returns = futureReturn.get();
              annualizedReturns.add(returns);
          } catch (ExecutionException e) {
              throw new StockQuoteServiceException("Error when calling API", e);
          }
        }

      Collections.sort(annualizedReturns, getComparator());
      return annualizedReturns;
  }

  public AnnualizedReturn getAnnualizedReturn(PortfolioTrade trade, LocalDate endDate) throws StockQuoteServiceException {
    LocalDate startDate = trade.getPurchaseDate();
    String symbol = trade.getSymbol(); 
  
  
    Double buyPrice = 0.0, sellPrice = 0.0;

    try {
      LocalDate startLocalDate = trade.getPurchaseDate();
      List<Candle> stocksStartToEndFull = getStockQuote(symbol, startLocalDate, endDate);


      Collections.sort(stocksStartToEndFull, (candle1, candle2) -> { 
        return candle1.getDate().compareTo(candle2.getDate()); 
      });
    
      Candle stockStartDate = stocksStartToEndFull.get(0);
      Candle stocksLatest = stocksStartToEndFull.get(stocksStartToEndFull.size() - 1);


      buyPrice = stockStartDate.getOpen();
      sellPrice = stocksLatest.getClose();
      endDate = stocksLatest.getDate();
    } catch (JsonProcessingException e) {
        throw new RuntimeException();
    }

    Double totalReturn = (sellPrice - buyPrice) / buyPrice;
    long daysBetweenPurchaseAndSelling = ChronoUnit.DAYS.between(startDate, endDate);
    Double totalYears = (double) (daysBetweenPurchaseAndSelling) / 365;
    Double annualizedReturn = Math.pow((1 + totalReturn), (1 / totalYears)) - 1;

    return new AnnualizedReturn(symbol, annualizedReturn, totalReturn);
  }


  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
  LocalDate endDate) throws StockQuoteServiceException, JsonProcessingException {
    List<AnnualizedReturn> annualizedReturns = new ArrayList<>();

    for (PortfolioTrade trade : portfolioTrades) {
        // buy price and sellPrice
        List<Candle> candlesList = getStockQuote(trade.getSymbol(), trade.getPurchaseDate(), endDate);
        double openingPrice = candlesList.get(0).getOpen();
        double closingPrice = candlesList.get(candlesList.size() - 1).getClose();
        annualizedReturns.add(getReturns(endDate, trade, openingPrice, closingPrice));
    }
    Collections.sort(annualizedReturns, getComparator());
    return annualizedReturns;
  }

  private AnnualizedReturn getReturns(LocalDate endDate,PortfolioTrade trade, Double buyPrice, Double sellPrice) {
      Double absReturns = (sellPrice - buyPrice) / buyPrice;
      Double yearsDiff = (double) trade.getPurchaseDate().until(endDate, ChronoUnit.DAYS) / 365;

      Double annReturns = Math.pow( (1 + absReturns), (1 / yearsDiff) ) - 1;

      return new AnnualizedReturn(trade.getSymbol(), annReturns, absReturns);
  }

  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
  throws JsonProcessingException, StockQuoteServiceException {

      return stockQuotesService.getStockQuote(symbol, from, to);

  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {

    String uriTemplate = "https:api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
        + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";
    return uriTemplate;
  }

}
