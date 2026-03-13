package com.open.spring.mvc.note;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.open.spring.mvc.person.Person;

public interface NoteJpaRepository extends JpaRepository<Note, Long> {
    List<Person> findByPersonId(Long id);

    @Transactional
    void deleteByPersonId(long id);
}

