package com.open.spring.mvc.cryptoMining;

import org.springframework.data.jpa.repository.JpaRepository;

import com.open.spring.mvc.person.Person;

import java.util.Optional;

public interface MiningUserRepository extends JpaRepository<MiningUser, Long> {
    Optional<MiningUser> findByPerson(Person person);
    Optional<MiningUser> findByPerson_Email(String email);  // Add this line
    //Optional<MiningUser> findByPerson_UID(String uid);
    boolean existsByPerson(Person person);
}