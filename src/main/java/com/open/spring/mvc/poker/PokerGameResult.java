package com.open.spring.mvc.poker;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PokerGameResult {
    private List<PokerCard> playerHand;
    private List<PokerCard> dealerHand;
    private double bet;

    public PokerGameResult(List<PokerCard> playerHand, List<PokerCard> dealerHand, double bet) {
        this.playerHand = playerHand;
        this.dealerHand = dealerHand;
        this.bet = bet;
        // Sort hands in descending order by rank value
        Collections.sort(this.playerHand, Collections.reverseOrder());
        Collections.sort(this.dealerHand, Collections.reverseOrder());
    }

    public List<PokerCard> getPlayerHand() {
        return playerHand;
    }

    public List<PokerCard> getDealerHand() {
        return dealerHand;
    }

    public double getBet() {
        return bet;
    }

    public boolean isPlayerWin() {
        return evaluateHand(playerHand) > evaluateHand(dealerHand);
    }

    public boolean isDealerWin() {
        return evaluateHand(dealerHand) > evaluateHand(playerHand);
    }

    public double getWinnings() {
        return bet * 2;
    }

    // Improved hand evaluation logic
    private int evaluateHand(List<PokerCard> hand) {
        if (isRoyalFlush(hand)) return 10;
        if (isStraightFlush(hand)) return 9;
        if (isFourOfAKind(hand)) return 8;
        if (isFullHouse(hand)) return 7;
        if (isFlush(hand)) return 6;
        if (isStraight(hand)) return 5;
        if (isThreeOfAKind(hand)) return 4;
        if (isTwoPair(hand)) return 3;
        if (isPair(hand)) return 2;
        return highCardValue(hand);
    }

    private boolean isRoyalFlush(List<PokerCard> hand) {
        return isStraightFlush(hand) && hand.get(0).getRank().equals("A");
    }

    private boolean isStraightFlush(List<PokerCard> hand) {
        return isFlush(hand) && isStraight(hand);
    }

    private boolean isFourOfAKind(List<PokerCard> hand) {
        return hasNOfAKind(hand, 4);
    }

    private boolean isFullHouse(List<PokerCard> hand) {
        Map<String, Integer> counts = getCardRankCounts(hand);
        return counts.containsValue(3) && counts.containsValue(2);
    }

    private boolean isFlush(List<PokerCard> hand) {
        String suit = hand.get(0).getSuit();
        for (PokerCard card : hand) {
            if (!card.getSuit().equals(suit)) return false;
        }
        return true;
    }

    private boolean isStraight(List<PokerCard> hand) {
        for (int i = 0; i < hand.size() - 1; i++) {
            if (hand.get(i).getCardRankValue() - hand.get(i + 1).getCardRankValue() != 1) {
                return false;
            }
        }
        return true;
    }

    private boolean isThreeOfAKind(List<PokerCard> hand) {
        return hasNOfAKind(hand, 3);
    }

    private boolean isTwoPair(List<PokerCard> hand) {
        int pairs = 0;
        Map<String, Integer> counts = getCardRankCounts(hand);
        for (int count : counts.values()) {
            if (count == 2) pairs++;
        }
        return pairs == 2;
    }

    private boolean isPair(List<PokerCard> hand) {
        return hasNOfAKind(hand, 2);
    }

    private boolean hasNOfAKind(List<PokerCard> hand, int n) {
        Map<String, Integer> counts = getCardRankCounts(hand);
        return counts.containsValue(n);
    }

    private Map<String, Integer> getCardRankCounts(List<PokerCard> hand) {
        Map<String, Integer> counts = new HashMap<>();
        for (PokerCard card : hand) {
            counts.put(card.getRank(), counts.getOrDefault(card.getRank(), 0) + 1);
        }
        return counts;
    }

    private int highCardValue(List<PokerCard> hand) {
        return hand.get(0).getCardRankValue(); // Assumes sorted in descending order
    }

    @Override
    public String toString() {
        return "GameResult{" +
                "playerHand=" + playerHand +
                ", dealerHand=" + dealerHand +
                ", bet=" + bet +
                '}';
    }
}
