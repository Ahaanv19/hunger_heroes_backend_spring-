package com.open.spring.mvc.crypto;
// package com.open.spring.mvc.crypto;

// import java.util.HashMap;
// import java.util.Map;
// import java.util.List;
// import java.util.ArrayList;
// import java.util.Optional;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.web.bind.annotation.CrossOrigin;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.RestController;

// import com.open.spring.mvc.person.Person;
// import com.open.spring.mvc.person.PersonJpaRepository;

// @RestController
// @CrossOrigin(origins = {"*"})
// public class Teamsetup {

//     @Autowired
//     private PersonJpaRepository repository;

//     private Map<String, Double> userBalancesCache = new HashMap<>();
//     private long lastCacheUpdateTime = 0;
//     private final long CACHE_EXPIRY_TIME = 600000;

//     private Map<String, Integer> teams = new HashMap<>();
//     private final int TEAM_CAPACITY = 5;

//     @PostMapping("/team/join/{teamName}")
//     public ResponseEntity<String> joinTeam(@PathVariable String teamName, Authentication authentication) {
//         teams.putIfAbsent(teamName, 0);

//         if (teams.get(teamName) >= TEAM_CAPACITY) {
//             return new ResponseEntity<>("This team is full.", HttpStatus.BAD_REQUEST);
//         }

//         teams.put(teamName, teams.get(teamName) + 1);

//         UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//         String email = userDetails.getUsername();

//         Optional<Person> personOptional = repository.findByUid(email);
//         if (personOptional.isPresent()) {
//             Person person = personOptional.get();
//             person.setTeam(teamName);
//             repository.save(person);
//             updateUserBalanceCache();
//             return new ResponseEntity<>("You have successfully joined the " + teamName + " team.", HttpStatus.OK);
//         }

//         return new ResponseEntity<>("User not found.", HttpStatus.NOT_FOUND);
//     }

//     @GetMapping("/team/members/{teamName}")
//     public ResponseEntity<String> getTeamMembers(@PathVariable String teamName) {
//         Integer teamSize = teams.get(teamName);
//         if (teamSize == null) {
//             return new ResponseEntity<>("Team does not exist.", HttpStatus.NOT_FOUND);
//         }
//         return new ResponseEntity<>("Current team size: " + teamSize, HttpStatus.OK);
//     }

//     @PostMapping("/team/create/{teamName}")
//     public ResponseEntity<String> createTeam(@PathVariable String teamName) {
//         if (teams.containsKey(teamName)) {
//             return new ResponseEntity<>("Team already exists.", HttpStatus.BAD_REQUEST);
//         }

//         teams.put(teamName, 0);
//         return new ResponseEntity<>("Team " + teamName + " created successfully.", HttpStatus.OK);
//     }

//     @GetMapping("/users/balances")
//     public ResponseEntity<List<String>> getUsersBalances() {
//         if (System.currentTimeMillis() - lastCacheUpdateTime > CACHE_EXPIRY_TIME || userBalancesCache.isEmpty()) {
//             updateUserBalanceCache();
//         }
        
//         List<String> userBalances = new ArrayList<>();
//         for (Map.Entry<String, Double> entry : userBalancesCache.entrySet()) {
//             userBalances.add("User: " + entry.getKey() + ", Balance: " + entry.getValue());
//         }
        
//         return new ResponseEntity<>(userBalances, HttpStatus.OK);
//     }

//     private void updateUserBalanceCache() {
//         List<Person> people = repository.findAll();
//         userBalancesCache.clear();
        
//         for (Person person : people) {
//             userBalancesCache.put(person.getUid(), person.getBanks().getBalance());
//         }

//         lastCacheUpdateTime = System.currentTimeMillis();
//     }
// }