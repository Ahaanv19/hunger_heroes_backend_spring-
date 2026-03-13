package com.open.spring.mvc.rpg.games;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "UnifiedGame")
@Table(name = "games")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // common identifiers
    private Long personId;
    private String personUid;

    // type: "blackjack", "cryptomine", "casino", "bank"
    private String type;

    // transaction fields
    private String txId;
    private Double betAmount;
    private Double amount;
    private Double balance;
    private String result;
    private Boolean success;

    @Lob
    private String details; // JSON or notes

    private LocalDateTime createdAt;

    public static Game[] init() {
        Game g1 = new Game();
        g1.setPersonId(1L);
        g1.setPersonUid("uid-alice");
        g1.setType("blackjack");
        g1.setBetAmount(50.0);
        g1.setAmount(100.0);
        g1.setBalance(150.0);
        g1.setResult("blackjack");
        g1.setSuccess(Boolean.TRUE);
        g1.setDetails("{\"hand\": [\"A\", \"K\"]}");
        g1.setCreatedAt(LocalDateTime.now());

        Game g2 = new Game();
        g2.setPersonId(2L);
        g2.setPersonUid("uid-bob");
        g2.setType("cryptomine");
        g2.setAmount(0.75);
        g2.setBalance(12.5);
        g2.setResult("mined");
        g2.setSuccess(Boolean.TRUE);
        g2.setDetails("{\"hashrate\": 120}");
        g2.setCreatedAt(LocalDateTime.now());

        Game g3 = new Game();
        g3.setPersonId(3L);
        g3.setPersonUid("uid-charlie");
        g3.setType("casino");
        g3.setBetAmount(20.0);
        g3.setAmount(0.0);
        g3.setBalance(5.0);
        g3.setResult("lose");
        g3.setSuccess(Boolean.FALSE);
        g3.setDetails("{\"game\": \"slots\"}");
        g3.setCreatedAt(LocalDateTime.now());

        Game g4 = new Game();
        g4.setPersonId(4L);
        g4.setPersonUid("uid-dana");
        g4.setType("bank");
        g4.setAmount(200.0);
        g4.setBalance(1200.0);
        g4.setResult("deposit");
        g4.setSuccess(Boolean.TRUE);
        g4.setDetails("{\"method\": \"wire\"}");
        g4.setCreatedAt(LocalDateTime.now());

        return new Game[] { g1, g2, g3, g4 };
    }
}
