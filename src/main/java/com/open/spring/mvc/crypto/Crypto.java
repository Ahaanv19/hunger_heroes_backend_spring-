package com.open.spring.mvc.crypto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Crypto {
    private String symbol;
    private String name;
    private double price;
    private double changePercentage;
}