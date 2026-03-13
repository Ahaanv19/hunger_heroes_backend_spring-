package com.open.spring.mvc.poker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PokerBoard {
    private List<PokerCard> deck;
    private List<PokerCard> playerHand;
    private List<PokerCard> dealerHand;

    public PokerBoard() {
        deck = new ArrayList<>();
        initializeDeck();
        shuffleDeck();
        playerHand = new ArrayList<>();
        dealerHand = new ArrayList<>();
    }

    // Initialize deck with 52 cards
    private void initializeDeck() {
        String[] suits = {"♠", "♣", "♥", "♦"};
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
        for (String suit : suits) {
            for (String rank : ranks) {
                deck.add(new PokerCard(rank, suit));
            }
        }
    }

    // Shuffle the deck
    private void shuffleDeck() {
        Collections.shuffle(deck, new Random());
    }

    // Deal a hand to both player and dealer
    public void dealHands() {
        if (deck.size() < 10) { // Reset the deck if there aren’t enough cards
            deck.clear();
            initializeDeck();
            shuffleDeck();
        }
    
        playerHand.clear();
        dealerHand.clear();
    
        for (int i = 0; i < 5; i++) {
            playerHand.add(deck.remove(0));
            dealerHand.add(deck.remove(0));
        }
    }
    
    

    public List<PokerCard> getPlayerHand() {
        return playerHand;
    }

    public List<PokerCard> getDealerHand() {
        return dealerHand;
    }
}
