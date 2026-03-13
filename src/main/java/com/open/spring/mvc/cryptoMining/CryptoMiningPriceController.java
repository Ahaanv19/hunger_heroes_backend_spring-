package com.open.spring.mvc.cryptoMining;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@CrossOrigin(origins = "*")
public class CryptoMiningPriceController {
    
    private static final String COINGECKO_API_URL = 
        "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin,ethereum,ftx-token&vs_currencies=usd&include_24h_change=true";

    @GetMapping("/api/crypto/prices")
    public ResponseEntity<String> getCryptoPrices() {
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(COINGECKO_API_URL, String.class);
        return ResponseEntity.ok(result);
    }
}