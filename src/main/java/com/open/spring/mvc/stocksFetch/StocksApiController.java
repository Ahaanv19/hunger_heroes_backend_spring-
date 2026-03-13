package com.open.spring.mvc.stocksFetch;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/stocks")
public class StocksApiController {

    @Value("${yahoofinance.quotesquery1v8.enabled:false}")
    private boolean isV8Enabled;

    private static final List<String> USER_AGENTS = Arrays.asList(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Safari/605.1.15",
        "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0",
        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0 Safari/537.36",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148"
    );

    private static final Random random = new Random();

    private String getRandomUserAgent() {
        return USER_AGENTS.get(random.nextInt(USER_AGENTS.size()));
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<?> getStockBySymbol(@PathVariable String symbol) {
        String url = isV8Enabled
                ? "https://query1.finance.yahoo.com/v8/finance/chart/" + symbol
                : "https://query1.finance.yahoo.com/v8/finance/chart/" + symbol;

        RestTemplate restTemplate = new RestTemplate();

        int maxRetries = 3;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("User-Agent", getRandomUserAgent());
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

                if (response.getStatusCode() == HttpStatus.OK) {
                    System.out.println("Successfully fetched stock data for: " + symbol);
                    return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
                } else {
                    System.out.println("No stock data returned for symbol: " + symbol);
                    return new ResponseEntity<>("Stock not found for symbol: " + symbol, HttpStatus.NOT_FOUND);
                }

            } catch (HttpClientErrorException.TooManyRequests e) {
                retryCount++;
                System.out.println("Rate limited! Retrying... Attempt: " + retryCount);
                try {
                    TimeUnit.SECONDS.sleep((long) Math.pow(2, retryCount)); // Exponential backoff
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return new ResponseEntity<>("Thread interrupted while retrying", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } catch (Exception e) {
                System.out.println("Error occurred while fetching stock data: " + e.getMessage());
                e.printStackTrace();
                return new ResponseEntity<>("Failed to retrieve stock data for " + symbol, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<>("Too many failed attempts to fetch stock data for " + symbol, HttpStatus.TOO_MANY_REQUESTS);
    }
}
