package com.open.spring.mvc.comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List; 

public interface CommentJPA extends JpaRepository<Comment, Long> {
    List<Comment> findByAuthor(String author); // Method to find announcement by author

    List<Comment> findByAssignment(String assignment);

    List<Comment> findAllByOrderByTimestampDesc();
}
