package com.open.spring.mvc.blackjack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.open.spring.mvc.person.Person;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;

/**
 * The Blackjack class is representing a Blackjack game instance
 * It manages the game state, card deck, player and dealer hands, and score calculations
 */
@Entity
public class Blackjack {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;

    private String status; // "ACTIVE" or "INACTIVE"

    @Column(nullable = false)
    private double betAmount; // The amount the player bets for the game

    @Column(columnDefinition = "TEXT")
    private String gameState; // Serialized JSON representation of the game state

    @Transient
    private Map<String, Object> gameStateMap = new HashMap<>(); // Stores game state in memory

    /**
     * this part initializes and shuffles a new deck of cards for the game
     * (deck is stored in `gameStateMap`)
     */
    public void initializeDeck() {
        List<String> deck = generateDeck();
        Collections.shuffle(deck);
        gameStateMap.put("deck", deck);
        persistGameState();
    }

    /**
     * Deals two cards to both the player and the dealer from the deck
     * Updates the `gameStateMap` with the hands and calculates the initial scores
     */
    public void dealInitialHands() {
        List<String> deck = safeCastToList(gameStateMap.get("deck"));
        List<String> playerHand = new ArrayList<>();
        List<String> dealerHand = new ArrayList<>();

        playerHand.add(deck.remove(0)); // Draw two cards for the player
        playerHand.add(deck.remove(0));
        dealerHand.add(deck.remove(0)); // Draw two cards for the dealer
        dealerHand.add(deck.remove(0));

        gameStateMap.put("playerHand", playerHand);
        gameStateMap.put("dealerHand", dealerHand);
        gameStateMap.put("playerScore", calculateScore(playerHand));
        gameStateMap.put("dealerScore", calculateScore(dealerHand));
        persistGameState();
    }

    /**
     * This calculates the score of a given hand based on Blackjack rules
     * aces can be worth 1 or 11, so it changes depending on the amount the player currently has
     * and face cards (K, Q, J) are worth 10
     * @param hand The list of card strings (e.g., "10H", "AC")
     * @return The calculated score
     */
    public int calculateScore(List<String> hand) {
        int score = 0;
        int aces = 0;

        for (String card : hand) {
            String rank = card.substring(0, card.length() - 1);
            switch (rank) {
                case "A" -> {
                    aces++;
                    score += 11;
                }
                case "K", "Q", "J" -> score += 10;
                default -> score += Integer.parseInt(rank);
            }
        }

        // Adjust score if it exceeds 21 and there are aces
        while (score > 21 && aces > 0) {
            score -= 10;
            aces--;
        }

        return score;
    }

    /**
     * Generates a full deck of 52 cards (without jokers).
     * 
     * @return A shuffled list of card strings.
     */
    private List<String> generateDeck() {
        String[] suits = {"H", "D", "C", "S"}; // Hearts, Diamonds, Clubs, Spades
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
        List<String> deck = new ArrayList<>();

        for (String suit : suits) {
            for (String rank : ranks) {
                deck.add(rank + suit);
            }
        }

        return deck;
    }

    /**
     * Loads the game state from the stored JSON string into `gameStateMap`.
     */
    public void loadGameState() {
        if (this.gameStateMap.isEmpty() && this.gameState != null) {
            this.gameStateMap = fromJsonString(this.gameState);
        }
    }

    /**
     * saves the current game state into a JSON string for persistencee
     */
    public void persistGameState() {
        this.gameState = toJsonString(this.gameStateMap);
    }

    /**
     * Converts a `Map` to a JSON string.
     *
     * @param map The game state map.
     * @return A JSON string representation of the map.
     */
    private String toJsonString(Map<String, Object> map) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert map to JSON string", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fromJsonString(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, HashMap.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON string to map", e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> safeCastToList(Object obj) {
        if (obj instanceof List) {
            return (List<String>) obj;
        }
        return new ArrayList<>();
    }

    // Getters and Setters

    /**
     * @return The ID of the blackjack game.
     */
    public Long getId() {
        return id;
    }

    /**
     * @return The person associated with this blackjack game.
     */
    public Person getPerson() {
        return person;
    }

    /**
     * Sets the player associated with this game.
     * 
     * @param person The player.
     */
    public void setPerson(Person person) {
        this.person = person;
    }

    /**
     * @return The current status of the game ("ACTIVE" or "INACTIVE").
     */
    public String getStatus() {
        return status;
    }

    /**
     * Updates the status of the game.
     * 
     * @param status "ACTIVE" or "INACTIVE".
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return The bet amount for the game.
     */
    public double getBetAmount() {
        return betAmount;
    }

    /**
     * Updates the bet amount.
     * 
     * @param betAmount The amount the player is betting.
     */
    public void setBetAmount(double betAmount) {
        this.betAmount = betAmount;
    }

    /**
     * @return The game state in JSON format.
     */
    public String getGameState() {
        return gameState;
    }

    /**
     * Updates the game state from a JSON string.
     * 
     * @param gameState The game state in JSON format.
     */
    public void setGameState(String gameState) {
        this.gameState = gameState;
    }

    /**
     * @return The in-memory game state map.
     */
    public Map<String, Object> getGameStateMap() {
        return gameStateMap;
    }

    /**
     * Updates the game state map and persists it to JSON.
     * 
     * @param gameStateMap The new game state map.
     */
    public void setGameStateMap(Map<String, Object> gameStateMap) {
        this.gameStateMap = gameStateMap;
        persistGameState();
    }
}