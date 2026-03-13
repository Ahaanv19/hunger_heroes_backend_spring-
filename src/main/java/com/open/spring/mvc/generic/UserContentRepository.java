package com.open.spring.mvc.generic;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserContentRepository extends JpaRepository<UserContent, Long> {
    List<UserContent> findByType(ContentType type);

    List<UserContent> findByUserIdAndType(String userId, ContentType type);
}
