package com.open.spring.mvc.blackjack;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.open.spring.mvc.person.Person;

public interface BlackjackJpaRepository extends JpaRepository<Blackjack, Long> {
    Optional<Blackjack> findFirstByPersonAndStatusOrderByIdDesc(Person person, String status);
}
