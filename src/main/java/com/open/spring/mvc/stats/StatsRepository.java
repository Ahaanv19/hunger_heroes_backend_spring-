package com.open.spring.mvc.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StatsRepository extends JpaRepository<Stats, Long> {
    /**
     * Finds a Stats entry by username.
     * We use Optional in case the user doesn't exist.
     */
    List<Stats> findAllByUsername(String username);

    List<Stats> findAllByUsernameAndModule(String username, String module);

    Optional<Stats> findByUsernameAndModuleAndSubmodule(String username, String module, int submodule);
}
