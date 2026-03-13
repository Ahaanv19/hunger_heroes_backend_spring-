
package com.open.spring.mvc.bathroom;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.Getter;


@RestController
@RequestMapping("/api/issue")
public class IssueApiController {
    @Autowired
    private IssueJPARepository repository;

    // @PostMapping("/add/{issueName}")
    // public ResponseEntity<Object> addToIssue(@PathVariable String issueName)
    // {
    //     Issue issue = new Issue(issueName, 0);
    //     repository.save(issue);
    //     return new ResponseEntity<>(issue, HttpStatus.OK);        
    // }
    @Getter
    public static class IssueDto
    {
        private String bathroomName;
        private int count;
        private String issue;
        private float positionX;
        private float positionY;
    }
    @PostMapping("/add")
    public ResponseEntity<Object> addIssue(@RequestBody IssueDto issueDto)
    {
        List<Issue> issues = repository.findByIssueAndBathroomIgnoreCase(issueDto.getIssue(), issueDto.getBathroomName());
        if(issues.size()==0)
        {
            Issue issue = new Issue(issueDto.getBathroomName(), issueDto.getIssue(), issueDto.getCount(), issueDto.getPositionX(), issueDto.getPositionY());
            repository.save(issue);
            return new ResponseEntity<>("Issue at: " + issueDto.getBathroomName() + " for " + issueDto.getIssue() + "has been successfully created", HttpStatus.CREATED);
        }
        else {
            Optional<Issue> optionalIssue = repository.findByIssueAndBathroom(issueDto.getIssue(), issueDto.getBathroomName());
            Issue report = optionalIssue.get();

            report.setCount(report.getCount() + 1);
            repository.save(report);
            return new ResponseEntity<>(report, HttpStatus.OK);
        }
    }

    @Getter
    public static class UpdateDto
    {
        private String bathroomName;
        private String issue;
    }

    @PutMapping("/update")
    public ResponseEntity<Object> updateIssue(@RequestBody UpdateDto updateDto)
    {
        Optional<Issue> optionalIssue = repository.findByIssueAndBathroom(updateDto.getIssue(), updateDto.getBathroomName());
        if(optionalIssue.isPresent())
        {
            Issue report = optionalIssue.get();

            report.setCount(report.getCount() + 1);
            repository.save(report);
            return new ResponseEntity<>(report, HttpStatus.OK);
        }
        return new ResponseEntity<>("Issue not found", HttpStatus.NOT_FOUND);
    }

    @GetMapping("/issues")
    public ResponseEntity<List<Issue>> getCurrentIssues()
    {
        return new ResponseEntity<>(repository.findAll(), HttpStatus.OK);
    }
}