package com.open.spring.mvc.blackjack;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.open.spring.mvc.bank.Bank;
import com.open.spring.mvc.bank.BankJpaRepository;
import com.open.spring.mvc.person.Person;
import com.open.spring.mvc.person.PersonJpaRepository;

@RestController
@RequestMapping("/api/casino/blackjack")
public class BlackjackApiController {

    private static final Logger LOGGER = Logger.getLogger(BlackjackApiController.class.getName());

    @Autowired
    private BlackjackJpaRepository repository;

    @Autowired
    private PersonJpaRepository personJpaRepository;

    @Autowired
    private BankJpaRepository bankJpaRepository;

    @PostMapping("/start")
    public ResponseEntity<Blackjack> startGame(@RequestBody Map<String, Object> request) {
        try {
            String uid = request.get("uid").toString();
            double betAmount = Double.parseDouble(request.get("betAmount").toString());

            Person person = personJpaRepository.findByUid(uid);
            Bank bank = bankJpaRepository.findByUid(uid);
            
            if (person == null || bank == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            // Check for existing active game
            Optional<Blackjack> existingGame = repository.findFirstByPersonAndStatusOrderByIdDesc(person, "ACTIVE");
            if (existingGame.isPresent()) {
                return ResponseEntity.ok(existingGame.get());
            }

            // Check balance
            if (bank.getBalance() < betAmount) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            // Create new game
            Blackjack game = new Blackjack();
            game.setPerson(person);
            game.setStatus("ACTIVE");
            game.setBetAmount(betAmount);
            game.initializeDeck();
            game.dealInitialHands();

            repository.save(game);
            
            // Add balance to response
            game.getGameStateMap().put("balance", bank.getBalance());
            game.persistGameState();
            
            return ResponseEntity.ok(game);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error starting game", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/hit")
    public ResponseEntity<Object> hit(@RequestBody Map<String, Object> request) {
        try {
            String uid = request.get("uid").toString();
            Person person = personJpaRepository.findByUid(uid);
            Bank bank = bankJpaRepository.findByUid(uid);

            if (person == null || bank == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Person or bank account not found");
            }

            Optional<Blackjack> optionalGame = repository.findFirstByPersonAndStatusOrderByIdDesc(person, "ACTIVE");
            if (optionalGame.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No active game found");
            }

            Blackjack game = optionalGame.get();
            game.loadGameState();

            List<String> playerHand = (List<String>) game.getGameStateMap().get("playerHand");
            List<String> deck = (List<String>) game.getGameStateMap().get("deck");

            if (deck == null || deck.isEmpty()) {
                return ResponseEntity.ok("Deck is empty");
            }

            // Draw card and update score
            String drawnCard = deck.remove(0);
            playerHand.add(drawnCard);
            int playerScore = game.calculateScore(playerHand);

            // Update game state
            game.getGameStateMap().put("playerHand", playerHand);
            game.getGameStateMap().put("playerScore", playerScore);
            game.getGameStateMap().put("deck", deck);

            // Check for 5-card Charlie (automatic win)
            boolean fiveCardCharlie = playerHand.size() >= 5 && playerScore <= 21;
            
            if (fiveCardCharlie) {
                game.getGameStateMap().put("result", "WIN");
                double updatedBalance = bank.getBalance() + game.getBetAmount();
                bank.setBalance(updatedBalance);
                bankJpaRepository.save(bank);
                game.setStatus("INACTIVE");
            } 
            // Check for bust
            else if (playerScore > 21) {
                game.getGameStateMap().put("result", "LOSE");
                double updatedBalance = bank.getBalance() - game.getBetAmount();
                bank.setBalance(updatedBalance);
                bankJpaRepository.save(bank);
                game.setStatus("INACTIVE");
            }

            // Update balance in response
            game.getGameStateMap().put("balance", bank.getBalance());
            game.persistGameState();
            
            repository.save(game);
            
            return ResponseEntity.ok(game);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing hit", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @PostMapping("/stand")
    public ResponseEntity<Object> stand(@RequestBody Map<String, Object> request) {
        try {
            String uid = request.get("uid").toString();
            Person person = personJpaRepository.findByUid(uid);
            Bank bank = bankJpaRepository.findByUid(uid);

            if (person == null || bank == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Person or bank account not found");
            }

            Optional<Blackjack> optionalGame = repository.findFirstByPersonAndStatusOrderByIdDesc(person, "ACTIVE");
            if (optionalGame.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No active game found");
            }

            Blackjack game = optionalGame.get();
            game.loadGameState();

            List<String> dealerHand = (List<String>) game.getGameStateMap().get("dealerHand");
            List<String> deck = (List<String>) game.getGameStateMap().get("deck");
            int playerScore = (int) game.getGameStateMap().getOrDefault("playerScore", 0);
            int dealerScore = (int) game.getGameStateMap().getOrDefault("dealerScore", 0);
            double betAmount = game.getBetAmount();

            // Dealer draws until score is at least 17
            while (dealerScore < 17 && deck != null && !deck.isEmpty()) {
                String drawnCard = deck.remove(0);
                dealerHand.add(drawnCard);
                dealerScore = game.calculateScore(dealerHand);
            }

            // Update game state
            game.getGameStateMap().put("dealerHand", dealerHand);
            game.getGameStateMap().put("dealerScore", dealerScore);
            game.getGameStateMap().put("deck", deck);

            // Determine game result
            String result;
            if (playerScore > 21) {
                result = "LOSE";
                double updatedBalance = bank.getBalance() - betAmount;
                bank.setBalance(updatedBalance);
                bankJpaRepository.save(bank);
            } 
            // 5-card Charlie (already handled in hit, but just in case)
            else if (playerScore <= 21 && ((List<String>)game.getGameStateMap().get("playerHand")).size() >= 5) {
                result = "WIN";
                double updatedBalance = bank.getBalance() + betAmount;
                bank.setBalance(updatedBalance, "blackjack");
                bankJpaRepository.save(bank);
            }
            else if (dealerScore > 21 || playerScore > dealerScore) {
                result = "WIN";
                double updatedBalance = bank.getBalance() + betAmount;
                bank.setBalance(updatedBalance, "blackjack");
                bankJpaRepository.save(bank);
            } else if (playerScore < dealerScore) {
                result = "LOSE";
                double updatedBalance = bank.getBalance() - betAmount;
                bank.setBalance(updatedBalance, "blackjack");
                bankJpaRepository.save(bank);
            } else {
                result = "DRAW";
            }

            // Update game state and save
            game.getGameStateMap().put("result", result);
            game.getGameStateMap().put("balance", bank.getBalance());
            game.setStatus("INACTIVE");
            game.persistGameState();
            
            repository.save(game);

            return ResponseEntity.ok(game);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing stand", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }
}