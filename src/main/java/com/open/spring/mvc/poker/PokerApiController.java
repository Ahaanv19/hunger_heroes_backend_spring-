package com.open.spring.mvc.poker;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.open.spring.mvc.bank.Bank;
import com.open.spring.mvc.bank.BankJpaRepository;

@RestController
@RequestMapping("/api/casino/poker")
public class PokerApiController {

    @Autowired
    private BankJpaRepository bankJpaRepository;

    private PokerBoard pokerBoard;

    // Request class to receive bet and uid from the client
    public static class PokerRequest {
        private double bet;
        private String uid;

        public double getBet() { return bet; }
        public void setBet(double bet) { this.bet = bet; }

        public String getUid() { return uid; }
        public void setUid(String uid) { this.uid = uid; }
    }

    // Response class to send game results back to the client
    public static class PokerResponse {
        private List<PokerCard> playerHand;
        private List<PokerCard> dealerHand;
        private double updatedBalance;
        private boolean playerWin;
        private double bet;

        public PokerResponse(List<PokerCard> playerHand, List<PokerCard> dealerHand, double updatedBalance, boolean playerWin, double bet) {
            this.playerHand = playerHand;
            this.dealerHand = dealerHand;
            this.updatedBalance = updatedBalance;
            this.playerWin = playerWin;
            this.bet = bet;
        }

        public List<PokerCard> getPlayerHand() { return playerHand; }
        public List<PokerCard> getDealerHand() { return dealerHand; }
        public double getUpdatedBalance() { return updatedBalance; }
        public boolean isPlayerWin() { return playerWin; }
        public double getBet() { return bet; }
    }

    @PostMapping("/play")
    public ResponseEntity<?> playGame(@RequestBody PokerRequest pokerRequest) {
        if (pokerBoard == null) {
            pokerBoard = new PokerBoard();  // Initialize if not done
        }

        // Fetch bank account by username
        Bank bank = bankJpaRepository.findByUid(pokerRequest.getUid());
        if (bank == null) {
            return new ResponseEntity<>("Bank account not found.", HttpStatus.NOT_FOUND);
        }

        double currentBalance = bank.getBalance();
        double bet = pokerRequest.getBet();

        // Check if the player has enough balance to place the bet
        if (bet > currentBalance) {
            return new ResponseEntity<>("Insufficient balance to place bet.", HttpStatus.BAD_REQUEST);
        }

        // Deal hands for the game
        pokerBoard.dealHands();

        // Determine game outcome
        PokerGameResult result = new PokerGameResult(pokerBoard.getPlayerHand(), pokerBoard.getDealerHand(), bet);
        boolean playerWin = result.isPlayerWin();
        double winnings = playerWin ? bet : -bet;
        double updatedBalance = currentBalance + winnings;

        // Update balance using Bank entity
        bank.setBalance(updatedBalance, "poker");
        bankJpaRepository.save(bank);

        // Send game results
        PokerResponse response = new PokerResponse(
            pokerBoard.getPlayerHand(),
            pokerBoard.getDealerHand(),
            updatedBalance,
            playerWin,
            bet
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetGame() {
        pokerBoard = new PokerBoard();
        return new ResponseEntity<>("Game has been reset.", HttpStatus.OK);
    }
}
