package com.open.spring.mvc.bathroom;

import java.util.Optional;
import java.util.List;

// import org.hibernate.mapping.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueJPARepository extends JpaRepository<Issue, Long> {
    Optional<Issue> findByIssueAndBathroom(String issue, String bathroom);
    List<Issue> findByIssueAndBathroomIgnoreCase(String issue, String bathroom);

}
