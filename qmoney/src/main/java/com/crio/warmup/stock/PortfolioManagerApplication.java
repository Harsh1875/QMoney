
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.nio.file.Files;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.util.FileUtils;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {

  private final static String token = "1135a69fa31f4622bd5b50f6854acbdfa55ee1ec"; 

  public static String getToken() {
    return token;
  }
  
  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {

    List<String> jsonToObject = new ArrayList<>();
    ObjectMapper objMap = getObjectMapper();

    PortfolioTrade[] trds = objMap.readValue(resolveFileFromResources(args[0]), PortfolioTrade[].class);

    for (PortfolioTrade t : trds) {
      jsonToObject.add(t.getSymbol());
    }

    return jsonToObject;
  }

  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }


  public static List<String> debugOutputs() {

     String valueOfArgument0 = "trades.json";
     String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/harsh3030-hc-ME_QMONEY_V2/qmoney/bin/main/trades.json";
     String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@5542c4ed";
     String functionNameFromTestFileInStackTrace = "PortfolioManagerApplicationTest.mainReadFile()";
     String lineNumberFromTestFileInStackTrace = "29";

    return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
        toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
        lineNumberFromTestFileInStackTrace});
  }


  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {

      ObjectMapper objectMapper = getObjectMapper();
      List<PortfolioTrade> trds = Arrays.asList(objectMapper.readValue(resolveFileFromResources(args[0]), PortfolioTrade[].class));
      List<TotalReturnsDto> sortedByValue = mainReadQuotesHelper(args, trds);
      Collections.sort(sortedByValue, TotalReturnsDto.closingComparator);
      List<String> stocks = new ArrayList<>();

      for (TotalReturnsDto t : sortedByValue) {
          stocks.add(t.getSymbol());
      }
      return stocks;
  }

  public static List<TotalReturnsDto> mainReadQuotesHelper(String[] args, List<PortfolioTrade> trds) {
      RestTemplate restTemplate = new RestTemplate();
      List<TotalReturnsDto> test = new ArrayList<>();

      for (PortfolioTrade t : trds) {
          String uri = "https://api.tiingo.com/tiingo/daily/" + t.getSymbol() + "/prices?startDate=" + t.getPurchaseDate() + 
                        "&endDate=" + args[1] + "&token=1135a69fa31f4622bd5b50f6854acbdfa55ee1ec";
          TiingoCandle[] result = restTemplate.getForObject(uri, TiingoCandle[].class);
          if (result != null) {
            test.add(new TotalReturnsDto(t.getSymbol(), result[result.length - 1].getClose()));
          }
      }
      return test;
  }

  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
      List<PortfolioTrade> trd = new ArrayList<>();

      ObjectMapper objMap = getObjectMapper();
      PortfolioTrade[] trds = objMap.readValue(resolveFileFromResources(filename), PortfolioTrade[].class);

      for (PortfolioTrade t : trds) {
          trd.add(t);
      }
      return trd;
  }

  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
     String prepareUrl = "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices?startDate="
                          + trade.getPurchaseDate() + "&endDate=" + endDate + "&token=" + token;

     return prepareUrl;
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Now that you have the list of PortfolioTrade and their data, calculate annualized returns
  //  for the stocks provided in the Json.
  //  Use the function you just wrote #calculateAnnualizedReturns.
  //  Return the list of AnnualizedReturns sorted by annualizedReturns in descending order.

  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.


  // TODO:
  //  Ensure all tests are passing using below command
  //  ./gradlew test --tests ModuleThreeRefactorTest
  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
     return candles.get(0).getOpen();
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
     return candles.get(candles.size() - 1).getClose();
  }


  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
      String uri = prepareUrl(trade, endDate, token);
      
      RestTemplate restTemplate = new RestTemplate();

      return Arrays.asList(restTemplate.getForObject(uri, TiingoCandle[].class));
      
  }

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {

        List<AnnualizedReturn> annualizedReturnsList = new ArrayList<>();

        ObjectMapper objMapper = getObjectMapper();
        PortfolioTrade[] trade = objMapper.readValue(resolveFileFromResources(args[0]), PortfolioTrade[].class);
        
        String date = args[1];
        LocalDate endDate = LocalDate.parse(date);

        for (PortfolioTrade trd : trade) {
            List<Candle> tempCandles = fetchCandles(trd, endDate, token);
            double closingPrice = getClosingPriceOnEndDate(tempCandles);
            double openingPrice = getOpeningPriceOnStartDate(tempCandles);
            annualizedReturnsList.add(calculateAnnualizedReturns(endDate, trd, openingPrice, closingPrice));
        }

        Comparator<AnnualizedReturn> sortByReturns = new Comparator<AnnualizedReturn>() {
              public int compare(AnnualizedReturn t1, AnnualizedReturn t2) {
                  return t1.getAnnualizedReturn().compareTo(t2.getAnnualizedReturn());
              }
        };

        Collections.sort(annualizedReturnsList, sortByReturns);
        Collections.reverse(annualizedReturnsList);

        return annualizedReturnsList;
  }

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {

        double totalYears = (double) trade.getPurchaseDate().until(endDate, ChronoUnit.DAYS)/365;
        //double totalYears = (double) (endDate.getYear() - trade.getPurchaseDate().getYear());
        double totalReturns = (sellPrice - buyPrice) / buyPrice;
        double annualizedReturn = Math.pow( (1 + totalReturns) , (1 / totalYears) ) - 1;

        return new AnnualizedReturn(trade.getSymbol(), annualizedReturn, totalReturns);
  }



  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
       String file = args[0];
       LocalDate endDate = LocalDate.parse(args[1]);
       //String contents = readFileAsString(file);
       ObjectMapper objectMapper = getObjectMapper();
       PortfolioTrade[] portfolioTrades = objectMapper.readValue(file, PortfolioTrade[].class);

       RestTemplate restTemplate = new RestTemplate();
       PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager(restTemplate);
       
       return portfolioManager.calculateAnnualizedReturn(Arrays.asList(portfolioTrades), endDate);
  }


  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    printJsonObject(mainReadFile(args));
    printJsonObject(mainReadQuotes(args));
    printJsonObject(mainCalculateSingleReturn(args));




    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }
}

