package com.open.spring.mvc.person;

import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface PersonRoleJpaRepository extends JpaRepository<PersonRole, Long> {
    PersonRole findByName(String name);
    List<PersonRole> findAll();
}