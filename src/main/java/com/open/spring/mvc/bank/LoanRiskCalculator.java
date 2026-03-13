package com.open.spring.mvc.bank;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class LoanRiskCalculator {
    // Feature weights (like ML coefficients)
    private static final double[] WEIGHTS = {
        0.42,  // Casino activity frequency
        0.38,  // Casino profit/loss ratio
        0.35,  // Recent casino activity (last 7 days)
        0.25,  // Loan to balance ratio
        0.20,  // Loan repayment history
        -0.30, // Stock activity (negative weight reduces risk)
        -0.28, // Crypto activity (negative weight reduces risk)
        0.15,  // Volatility of returns
        0.22   // Account balance trend
    };
    
    // Bias term (intercept in ML model)
    private static final double BIAS = 2.5;
    
    // Sigmoid activation function parameters
    private static final double SIGMOID_STEEPNESS = 2.0;
    
    // Output range parameters
    private static final double MIN_INTEREST_RATE = 1.0;
    private static final double MAX_INTEREST_RATE = 5.0;
    
    // Regularization parameter (prevents overfitting)
    private static final double REGULARIZATION = 0.05;
    
    // Risk categories with their base weights
    private static final Map<String, Double> ACTIVITY_RISK_WEIGHTS = Map.of(
        "casino_dice", 0.85,
        "casino_poker", 0.80,
        "casino_mines", 0.95,
        "casino_blackjack", 0.82,
        "stocks", 0.30,
        "cryptomining", 0.35
    );
    
    /**
     * Calculate daily interest rate using ML techniques
     * @param bank The bank object containing user data
     * @return Daily interest rate between 1-5%
     */
    public static double calculateDailyInterestRate(Bank bank) {
        if (bank == null || bank.getProfitMap() == null) {
            return BIAS;
        }
        
        // Extract features (like ML feature extraction)
        double[] features = extractFeatures(bank);
        
        // Apply the model (weighted sum + bias)
        double weightedSum = BIAS;
        for (int i = 0; i < features.length; i++) {
            weightedSum += features[i] * WEIGHTS[i];
        }
        
        // Apply regularization (L2 regularization)
        double regularizationTerm = 0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            regularizationTerm += WEIGHTS[i] * WEIGHTS[i];
        }
        weightedSum -= REGULARIZATION * regularizationTerm;
        
        // Apply activation function (sigmoid)
        double interestRate = sigmoid(weightedSum, SIGMOID_STEEPNESS);
        
        // Scale to desired range
        interestRate = MIN_INTEREST_RATE + (MAX_INTEREST_RATE - MIN_INTEREST_RATE) * interestRate;
        
        // Round to 2 decimal places
        return Math.round(interestRate * 100.0) / 100.0;
    }
    
    /**
     * Extract ML features from bank data
     */
    private static double[] extractFeatures(Bank bank) {
        Map<String, List<List<Object>>> profitMap = bank.getProfitMap();
        double balance = bank.getBalance();
        double loanAmount = bank.getLoanAmount();
        
        // Feature 1: Casino activity frequency
        double casinoFrequency = extractCasinoFrequency(profitMap);
        
        // Feature 2: Casino profit/loss ratio
        double casinoProfitLossRatio = extractCasinoProfitLossRatio(profitMap);
        
        // Feature 3: Recent casino activity (last 7 days)
        double recentCasinoActivity = extractRecentCasinoActivity(profitMap);
        
        // Feature 4: Loan to balance ratio
        double loanToBalanceRatio = balance > 0 ? loanAmount / balance : 2.0;
        loanToBalanceRatio = Math.min(loanToBalanceRatio, 2.0); // Cap at 200%
        
        // Feature 5: Loan repayment history (placeholder - use random for demo)
        // In real implementation, track loan payments
        double loanRepaymentScore = 0.5; // Neutral score
        
        // Feature 6: Stock activity
        double stockActivity = extractStockActivity(profitMap);
        
        // Feature 7: Crypto activity
        double cryptoActivity = extractCryptoActivity(profitMap);
        
        // Feature 8: Volatility of returns
        double volatility = calculateVolatility(profitMap);
        
        // Feature 9: Account balance trend
        double balanceTrend = 0.5; // Neutral
        
        return new double[] { 
            casinoFrequency, 
            casinoProfitLossRatio, 
            recentCasinoActivity,
            loanToBalanceRatio, 
            loanRepaymentScore, 
            stockActivity, 
            cryptoActivity,
            volatility,
            balanceTrend
        };
    }
    
    /**
     * Calculate casino frequency (normalized)
     */
    private static double extractCasinoFrequency(Map<String, List<List<Object>>> profitMap) {
        int totalCasinoTransactions = 0;
        
        for (String activity : ACTIVITY_RISK_WEIGHTS.keySet()) {
            if (activity.startsWith("casino_")) {
                List<List<Object>> transactions = profitMap.getOrDefault(activity, List.of());
                totalCasinoTransactions += transactions.size();
            }
        }
        
        // Normalize: 0 = no activity, 1 = very high activity (20+ transactions)
        return Math.min(totalCasinoTransactions / 20.0, 1.0);
    }
    
    /**
     * Calculate casino profit/loss ratio
     * Returns value from 0 (all losses) to 1 (all profits)
     */
    private static double extractCasinoProfitLossRatio(Map<String, List<List<Object>>> profitMap) {
        double totalProfit = 0;
        double totalBet = 0;
        int transactions = 0;
        
        for (String activity : ACTIVITY_RISK_WEIGHTS.keySet()) {
            if (activity.startsWith("casino_")) {
                List<List<Object>> activityTransactions = profitMap.getOrDefault(activity, List.of());
                
                for (List<Object> transaction : activityTransactions) {
                    try {
                        double profit = Double.valueOf(transaction.get(1).toString());
                        // Estimate the bet amount (profit is net, so we need to determine original bet)
                        double estimatedBet = profit < 0 ? -profit : profit / 2; // Rough estimate
                        
                        totalProfit += profit;
                        totalBet += estimatedBet;
                        transactions++;
                    } catch (Exception e) {
                        // Skip invalid data
                    }
                }
            }
        }
        
        if (transactions == 0 || totalBet == 0) {
            return 0.5; // Neutral for no data
        }
        
        // Calculate ratio and normalize to 0-1 range
        double ratio = totalProfit / totalBet;
        // Map from -1 (all losses) to +1 (all wins) to 0-1 range
        return Math.min(Math.max((ratio + 1) / 2, 0), 1);
    }
    
    /**
     * Calculate recent casino activity (last 7 days)
     */
    private static double extractRecentCasinoActivity(Map<String, List<List<Object>>> profitMap) {
        int recentActivities = 0;
        Instant sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS);
        
        for (String activity : ACTIVITY_RISK_WEIGHTS.keySet()) {
            if (activity.startsWith("casino_")) {
                List<List<Object>> transactions = profitMap.getOrDefault(activity, List.of());
                
                for (List<Object> transaction : transactions) {
                    try {
                        String timeStr = (String) transaction.get(0);
                        Instant transactionTime = Instant.parse(timeStr);
                        if (transactionTime.isAfter(sevenDaysAgo)) {
                            recentActivities++;
                        }
                    } catch (Exception e) {
                        // Skip invalid data
                    }
                }
            }
        }
        
        // Normalize: 0 = no recent activity, 1 = very high activity (10+ transactions in 7 days)
        return Math.min(recentActivities / 10.0, 1.0);
    }
    
    /**
     * Calculate stock activity
     */
    private static double extractStockActivity(Map<String, List<List<Object>>> profitMap) {
        List<List<Object>> stockTransactions = profitMap.getOrDefault("stocks", List.of());
        
        if (stockTransactions.isEmpty()) {
            return 0.0; // No activity
        }
        
        // Calculate profit ratio
        double totalProfit = 0;
        double totalInvestment = 0;
        
        for (List<Object> transaction : stockTransactions) {
            try {
                double profit = Double.valueOf(transaction.get(1).toString());
                // Estimate investment (similar to casino bet estimation)
                double estimatedInvestment = profit < 0 ? -profit : profit * 4; // Rough estimate
                
                totalProfit += profit;
                totalInvestment += estimatedInvestment;
            } catch (Exception e) {
                // Skip invalid data
            }
        }
        
        // Activity level based on frequency and profitability
        double frequency = Math.min(stockTransactions.size() / 10.0, 1.0);
        double profitability = totalInvestment > 0 ? 
                Math.min(Math.max((totalProfit / totalInvestment + 0.5), 0), 1) : 0.5;
        
        // Combined score
        return (frequency * 0.4 + profitability * 0.6);
    }
    
    /**
     * Calculate crypto activity
     */
    private static double extractCryptoActivity(Map<String, List<List<Object>>> profitMap) {
        List<List<Object>> cryptoTransactions = profitMap.getOrDefault("cryptomining", List.of());
        
        if (cryptoTransactions.isEmpty()) {
            return 0.0; // No activity
        }
        
        // Calculate profit ratio
        double totalProfit = 0;
        double totalCost = 0;
        
        for (List<Object> transaction : cryptoTransactions) {
            try {
                double profit = Double.valueOf(transaction.get(1).toString());
                // Estimate cost
                double estimatedCost = profit < 0 ? -profit : profit * 3; // Rough estimate
                
                totalProfit += profit;
                totalCost += estimatedCost;
            } catch (Exception e) {
                // Skip invalid data
            }
        }
        
        // Activity level based on frequency and profitability
        double frequency = Math.min(cryptoTransactions.size() / 15.0, 1.0);
        double profitability = totalCost > 0 ? 
                Math.min(Math.max((totalProfit / totalCost + 0.5), 0), 1) : 0.5;
        
        // Combined score
        return (frequency * 0.3 + profitability * 0.7);
    }
    
    /**
     * Calculate volatility across all activities
     */
    private static double calculateVolatility(Map<String, List<List<Object>>> profitMap) {
        List<Double> allProfits = new ArrayList<>();
        
        // Collect all profit values
        for (String activity : ACTIVITY_RISK_WEIGHTS.keySet()) {
            List<List<Object>> transactions = profitMap.getOrDefault(activity, List.of());
            
            for (List<Object> transaction : transactions) {
                try {
                    double profit = Double.valueOf(transaction.get(1).toString());
                    allProfits.add(profit);
                } catch (Exception e) {
                    // Skip invalid data
                }
            }
        }
        
        if (allProfits.size() < 2) {
            return 0.5; // Not enough data, return neutral
        }
        
        // Calculate standard deviation (simplified)
        double sum = 0;
        for (Double profit : allProfits) {
            sum += profit;
        }
        double mean = sum / allProfits.size();
        
        double variance = 0;
        for (Double profit : allProfits) {
            variance += Math.pow(profit - mean, 2);
        }
        variance /= allProfits.size();
        
        double stdDev = Math.sqrt(variance);
        
        // Normalize to 0-1 range (0 = low volatility, 1 = high volatility)
        // Use coefficient of variation normalized to 0-1
        double coeffOfVariation = mean != 0 ? Math.abs(stdDev / mean) : stdDev;
        return Math.min(coeffOfVariation / 3.0, 1.0); // Cap at 3.0 for normalization
    }
    
    /**
     * Sigmoid activation function (logistic function)
     * Maps any input to a value between 0 and 1
     */
    private static double sigmoid(double x, double steepness) {
        return 1.0 / (1.0 + Math.exp(-steepness * x));
    }
    
    /**
     * Update Bank class to use the risk calculator
     */
    public static void addRiskBasedInterestToBank(Bank bank) {
        // Calculate personalized interest rate using ML model
        double personalizedRate = calculateDailyInterestRate(bank);
        
        // Update the bank's interest calculation method
        bank.setDailyInterestRate(personalizedRate);
    }
    
    /**
     * Classification model to determine risk category
     * Returns: 0 (low risk), 1 (medium risk), 2 (high risk)
     */
    public static int classifyRiskCategory(Bank bank) {
        double interestRate = calculateDailyInterestRate(bank);
        
        if (interestRate < 2.5) return 0;      // Low risk
        else if (interestRate < 3.8) return 1; // Medium risk
        else return 2;                        // High risk
    }
    
    /**
     * Apply ensemble method - combine multiple "models"
     * This simulates having multiple models and taking their weighted average
     */
    public static double ensembleInterestRate(Bank bank) {
        double baseRate = calculateDailyInterestRate(bank);
        
        // Simulate different models with slight variations
        double model2Rate = baseRate * (1 + (new Random().nextDouble() * 0.1 - 0.05));
        double model3Rate = baseRate * (1 + (new Random().nextDouble() * 0.15 - 0.075));
        
        // Weighted average (ensemble)
        double ensembleRate = (baseRate * 0.6) + (model2Rate * 0.25) + (model3Rate * 0.15);
        
        // Ensure within bounds
        return Math.max(MIN_INTEREST_RATE, Math.min(MAX_INTEREST_RATE, ensembleRate));
    }
}