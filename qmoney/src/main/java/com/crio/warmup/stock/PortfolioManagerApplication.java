
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
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
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {

  
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


  // TODO:
  //  Build the Url using given parameters and use this function in your code to cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
     String prepareUrl = "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices?startDate="
                          + trade.getPurchaseDate() + "&endDate=" + endDate + "&token=" + token;

     return prepareUrl;
  }



  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    printJsonObject(mainReadFile(args));

    printJsonObject(mainReadQuotes(args));
  }
}

