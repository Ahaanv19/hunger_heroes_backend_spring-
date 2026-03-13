package com.open.spring.mvc.rpg.adventure;

import java.time.LocalDateTime;

// imports trimmed to used ones
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
@Entity
@Table(name = "adventure")
public class Adventure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long personId;
    private String personUid;

    private Long questionId;
    private String questionTitle;
    @Lob
    private String questionContent;
    private String questionCategory;
    private Integer questionPoints;

    private Long choiceId;
    private String choiceText;
    private Boolean choiceIsCorrect;

    private Boolean answerIsCorrect;
    @Lob
    private String answerContent;
    private Long chatScore;

    private String rubricRuid;
    @Lob
    private String rubricCriteria;

    @Lob
    private String details; // JSON blob combining choice/answer/rubric/chat fields

    private Double balance;

    private LocalDateTime createdAt;

    // Initialize static test data for Adventure similar to Person.init()
    public static Adventure[] init() {
        Adventure a1 = new Adventure();
        a1.setPersonId(1L);
        a1.setPersonUid("uid-alice");
        a1.setQuestionId(101L);
        a1.setQuestionTitle("Intro Quest");
        a1.setQuestionContent("Solve the riddle of the Sphinx");
        a1.setQuestionCategory("riddle");
        a1.setQuestionPoints(10);
        a1.setChoiceId(1001L);
        a1.setChoiceText("Answer A");
        a1.setChoiceIsCorrect(Boolean.TRUE);
        a1.setAnswerIsCorrect(Boolean.TRUE);
        a1.setAnswerContent("I answered A");
        a1.setChatScore(50L);
        a1.setRubricRuid("ruid-1");
        a1.setRubricCriteria("Be precise");
        a1.setBalance(10.5);
        a1.setCreatedAt(LocalDateTime.now());

        Adventure a2 = new Adventure();
        a2.setPersonId(2L);
        a2.setPersonUid("uid-bob");
        a2.setQuestionId(102L);
        a2.setQuestionTitle("Forest Trial");
        a2.setQuestionContent("Find the hidden path");
        a2.setQuestionCategory("exploration");
        a2.setQuestionPoints(15);
        a2.setChoiceId(1002L);
        a2.setChoiceText("Left path");
        a2.setChoiceIsCorrect(Boolean.FALSE);
        a2.setAnswerIsCorrect(Boolean.FALSE);
        a2.setAnswerContent("I tried left");
        a2.setChatScore(20L);
        a2.setRubricRuid("ruid-2");
        a2.setRubricCriteria("Explain steps");
        a2.setBalance(5.0);
        a2.setCreatedAt(LocalDateTime.now());

        Adventure a3 = new Adventure();
        a3.setPersonId(3L);
        a3.setPersonUid("uid-charlie");
        a3.setQuestionId(103L);
        a3.setQuestionTitle("Cave Puzzle");
        a3.setQuestionContent("Arrange the runes");
        a3.setQuestionCategory("puzzle");
        a3.setQuestionPoints(20);
        a3.setChoiceId(1003L);
        a3.setChoiceText("Rune X");
        a3.setChoiceIsCorrect(Boolean.TRUE);
        a3.setAnswerIsCorrect(Boolean.TRUE);
        a3.setAnswerContent("Placed X");
        a3.setChatScore(75L);
        a3.setRubricRuid("ruid-3");
        a3.setRubricCriteria("Be exact");
        a3.setBalance(0.0);
        a3.setCreatedAt(LocalDateTime.now());

        Adventure a4 = new Adventure();
        a4.setPersonId(4L);
        a4.setPersonUid("uid-dana");
        a4.setQuestionId(104L);
        a4.setQuestionTitle("River Crossing");
        a4.setQuestionContent("Build a raft");
        a4.setQuestionCategory("skill");
        a4.setQuestionPoints(8);
        a4.setChoiceId(1004L);
        a4.setChoiceText("Use rope");
        a4.setChoiceIsCorrect(Boolean.FALSE);
        a4.setAnswerIsCorrect(Boolean.FALSE);
        a4.setAnswerContent("Used rope");
        a4.setChatScore(10L);
        a4.setRubricRuid("ruid-4");
        a4.setRubricCriteria("Use safe methods");
        a4.setBalance(2.5);
        a4.setCreatedAt(LocalDateTime.now());

        Adventure a5 = new Adventure();
        a5.setPersonId(5L);
        a5.setPersonUid("uid-ellen");
        a5.setQuestionId(105L);
        a5.setQuestionTitle("Final Gate");
        a5.setQuestionContent("Recite oath");
        a5.setQuestionCategory("finale");
        a5.setQuestionPoints(30);
        a5.setChoiceId(1005L);
        a5.setChoiceText("Oath B");
        a5.setChoiceIsCorrect(Boolean.TRUE);
        a5.setAnswerIsCorrect(Boolean.TRUE);
        a5.setAnswerContent("Spoke B");
        a5.setChatScore(120L);
        a5.setRubricRuid("ruid-5");
        a5.setRubricCriteria("Honor the oath");
        a5.setBalance(50.0);
        a5.setCreatedAt(LocalDateTime.now());

        return new Adventure[] { a1, a2, a3, a4, a5 };
    }
}
