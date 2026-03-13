package com.open.spring.mvc.dice;

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

import lombok.Getter;

@RestController
@RequestMapping("/api/casino/dice")
public class DiceApiController {

    private static final Logger LOGGER = Logger.getLogger(DiceApiController.class.getName());

    @Autowired
    private PersonJpaRepository personJpaRepository;
    
    @Autowired
    private BankJpaRepository bankJpaRepository;
    
    @Getter
    public static class DiceRequest {
        private double winChance;
        private double betSize;
        private String uid;
    }
    
    @PostMapping("/calculate")
    public ResponseEntity<Object> playDice(@RequestBody DiceRequest diceRequest) {
        try {
            String uid = diceRequest.getUid();
            double betSize = diceRequest.getBetSize();
            double winChance = diceRequest.getWinChance();
            
            LOGGER.info("Received dice request for user: " + uid + ", bet: " + betSize + ", win chance: " + winChance);
            
            // Find user and bank account
            Person person = personJpaRepository.findByUid(uid);
            Bank bank = bankJpaRepository.findByUid(uid);
            
            if (person == null || bank == null) {
                LOGGER.warning("Person or bank account not found for uid: " + uid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Person or bank account not found");
            }
            
            // Check if bet size is valid
            if (betSize <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bet size must be greater than zero");
            }
            
            // Check if user has enough balance
            if (betSize > bank.getBalance()) {
                LOGGER.warning("Insufficient balance for user: " + uid + ", balance: " + bank.getBalance() + ", bet: " + betSize);
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(bank.getBalance());
            }
            
            // Create dice game and calculate result
            Dice dice = new Dice(winChance, betSize);
            double winAmount = dice.calculateWin();
            
            // Update balance
            double currentBalance = bank.getBalance();
            double updatedBalance = currentBalance + winAmount;
            bank.setBalance(updatedBalance, "dice");
            bankJpaRepository.save(bank);
            
            LOGGER.info("Dice game completed for user: " + uid + ", result: " + winAmount + ", new balance: " + updatedBalance);
            
            // Return updated balance
            return ResponseEntity.ok(bank.getBalance());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing dice game", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }
}