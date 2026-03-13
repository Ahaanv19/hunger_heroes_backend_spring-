package com.open.spring.mvc.mortevision;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TasksJpaRepository extends JpaRepository<Tasks, Long> {
    // Additional query methods can be added here if needed
    List<Tasks> findByUsernameOrderByDueDateAsc(String username);
}
