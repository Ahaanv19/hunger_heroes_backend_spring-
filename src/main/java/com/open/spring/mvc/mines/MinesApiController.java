package com.open.spring.mvc.mines;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.open.spring.mvc.bank.Bank;
import com.open.spring.mvc.bank.BankJpaRepository;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/casino/mines")
public class MinesApiController {

    @Autowired
    private BankJpaRepository bankJpaRepository;

    private MinesBoard board;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MinesRequest {
        private double bet;
        private String uid;

        public double getBet() { return bet; }
        public void setBet(double bet) { this.bet = bet; }

        public String getUid() { return uid; }
        public void setUid(String uid) { this.uid = uid; }
    }

    @GetMapping("/{xCoord}/{yCoord}")
    public ResponseEntity<Boolean> getMine(@PathVariable int xCoord, @PathVariable int yCoord) {
        log.info("Checking mine at coordinates ({}, {})", xCoord, yCoord);
        return new ResponseEntity<>(board.checkMine(xCoord, yCoord), HttpStatus.OK);
    }

    @PostMapping("/winnings")
    public ResponseEntity<Double> calculateWinnings(@RequestBody MinesRequest minesRequest) {
        Bank bank = bankJpaRepository.findByUid(minesRequest.getUid());
        if (bank == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        double betSize = minesRequest.getBet();
        double winnings = board.winnings() * betSize;
        double updatedBalance = bank.getBalance() + winnings;

        bank.setBalance(updatedBalance, "mines");
        bankJpaRepository.save(bank);

        return new ResponseEntity<>(updatedBalance, HttpStatus.OK);
    }

    @PostMapping("/stakes/{stakes}")
    public ResponseEntity<Double> postStakes(@PathVariable String stakes, @RequestBody MinesRequest minesRequest) {
        Bank bank = bankJpaRepository.findByUid(minesRequest.getUid());
        if (bank == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        double betSize = minesRequest.getBet();
        double updatedBalance = bank.getBalance() - betSize;

        if (updatedBalance < 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        bank.setBalance(updatedBalance, "mines");
        bankJpaRepository.save(bank);

        board = new MinesBoard(stakes);

        return new ResponseEntity<>(updatedBalance, HttpStatus.OK);
    }

    @GetMapping("/balance/{uid}")
    public ResponseEntity<Double> getBalance(@PathVariable String uid) {
        Bank bank = bankJpaRepository.findByUid(uid);
        if (bank == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(bank.getBalance(), HttpStatus.OK);
    }

    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> handleMinesGame(@RequestBody MinesRequest minesRequest) {
        Bank bank = bankJpaRepository.findByUid(minesRequest.getUid());
        if (bank == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        double currentBalance = bank.getBalance();
        double updatedBalance = currentBalance + minesRequest.getBet();

        if (updatedBalance < 0) {
            return new ResponseEntity<>(
                Map.of("error", "Insufficient balance"),
                HttpStatus.BAD_REQUEST
            );
        }

        bank.setBalance(updatedBalance, "mines");
        bankJpaRepository.save(bank);

        return new ResponseEntity<>(
            Map.of(
                "status", "success",
                "updatedBalance", updatedBalance,
                "transactionAmount", minesRequest.getBet()
            ),
            HttpStatus.OK
        );
    }
}
