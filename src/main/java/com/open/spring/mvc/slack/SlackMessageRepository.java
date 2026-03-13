package com.open.spring.mvc.slack;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SlackMessageRepository extends JpaRepository<SlackMessage, LocalDateTime> {}
