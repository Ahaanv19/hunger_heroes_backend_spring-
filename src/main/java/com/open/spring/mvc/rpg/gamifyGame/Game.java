// Legacy GamifyGame removed — replaced by unified Adventure/Game APIs.
package com.open.spring.mvc.rpg.gamifyGame;
import com.open.spring.mvc.person.Person;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = false, nullable = true)
    private String name;

    // user uid
    @OneToOne
    @JoinColumn(name = "person_uid", nullable = false)
    private Person uid;

    public Game(String name, Person uid) {
        this.name = name;
        this.uid = uid;
    }

    public static Game createGame(String name, Person uid) {
        Game game = new Game();
        game.setName(name);
        game.setUid(uid);
        return game;
    }

    public static String[][] init() {
        return new String[][] {
  
            {"Adventure", "toby"},
            {"CSA Coders", "niko"},
            {"Dinosaur", "hop"},
            
            

        };
    }
}
