package com.open.spring.mvc.trains;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.open.spring.mvc.person.Person;
import com.open.spring.mvc.person.PersonDetailsService;

import lombok.Data;

@Controller
@RequestMapping("/api/train")
public class TrainCombinedApiController {
    @Autowired
    private PersonDetailsService personRepository;

    @Autowired
    private TrainJPARepository trainRepository;

    @Autowired
    private TrainStationJPARepository trainStationRepository;

    @Autowired
    private TrainCompanyJPARepository repository;

    //idk why this one only works with transactional annotation, its probally to do with the one-to-one relationship betweeen TrainCompany and Person
    @GetMapping("/get/company")
    @Transactional
    public ResponseEntity<TrainCompany> getCompany(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Person person = personRepository.getByUid(userDetails.getUsername());
        Long id = person.getId();

        if (!repository.existsById(id)) {
            // if a train company doesn't exist, then make one
            TrainCompany company = new TrainCompany();
            company.setCompanyName("Company " + id.toString());
            company.setBalance(100000f); //give a default balance of 100,000
            company.setOwner(person);
            repository.save(company);
        }

        TrainCompany company = repository.getById(id);
        
        ResponseEntity<TrainCompany> responseEntity = new ResponseEntity<TrainCompany>(company, HttpStatus.OK);
        return responseEntity;
    }

    @GetMapping("/get/trains")
    public ResponseEntity<List<Train>> getTrains(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Person person = personRepository.getByUid(userDetails.getUsername());
        Long id = person.getId();

        if (!repository.existsById(id)) {
            ResponseEntity<List<Train>> responseEntity = new ResponseEntity<List<Train>>(HttpStatus.FAILED_DEPENDENCY);
            return responseEntity;
        }

        TrainCompany company = repository.getById(id);

        if(!trainRepository.existsByCompanyId(company.getId())){
            Train train = Train.createTrain(); //creates an empty train with a randomized type/color
            train.setCompany(company);
            trainRepository.save(train);
        }
        
        List<Train> trains = trainRepository.getAllByCompanyId(company.getId());

        ResponseEntity<List<Train>> responseEntity = new ResponseEntity<List<Train>>(trains, HttpStatus.OK);
        return responseEntity;
    }

    @GetMapping("/get/station")
    @Transactional
    public ResponseEntity<TrainStation> getStation(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Person person = personRepository.getByUid(userDetails.getUsername());
        Long id = person.getId();

        if (!repository.existsById(id)) {
            ResponseEntity<TrainStation> responseEntity = new ResponseEntity<TrainStation>(HttpStatus.FAILED_DEPENDENCY);
            return responseEntity;
        }

        TrainCompany company = repository.getById(id);

        if(!trainStationRepository.existsById(company.getId())){
            TrainStation trainStation = new TrainStation();
            trainStation.setCompany(company);
            trainStation.setPosition(Float.valueOf((float)(Math.random()*20000)-10000)); //random position between -10000, 10000
            //////default product at station
            HashMap<String,List<Product>> map = new HashMap<String,List<Product>>();
            Product defaultProduct = new Product();
            defaultProduct.setName("Banana");
            defaultProduct.setPrice(Float.valueOf((float)0.62));
            defaultProduct.setDescription("A radioactive yellow fruit that grows on trees.");
            map.put("banana", List.of(defaultProduct));
            //////
            trainStation.setProducts(map);
           
            trainStation.setTerrain(Integer.valueOf((int)(Math.random()*11)));
            trainStationRepository.save(trainStation);
        }
        
        TrainStation trainStation = trainStationRepository.getById(company.getId());

        ResponseEntity<TrainStation> responseEntity = new ResponseEntity<TrainStation>(trainStation, HttpStatus.OK);
        return responseEntity;
    }

    @GetMapping("/get/stations/byProduct/{productName}")
    public ResponseEntity<List<TrainStation>> getStationsWithProduct(@PathVariable String productName) {
        String param = productName.toLowerCase(); //keys should always be lower case
        List<TrainStation> stations = trainStationRepository.findAll();
        List<TrainStation> stationsWithProduct = new ArrayList<TrainStation>(0);
        for(int i = 0; i<stations.size(); i++){
            TrainStation station = stations.get(i);
            Map<String, List<Product>> products = station.getProducts();
            if(products.containsKey(param)){
                if(products.get(param).size() > 0){
                    stationsWithProduct.add(station);
                }
            }
        }

        ResponseEntity<List<TrainStation>> responseEntity = new ResponseEntity<List<TrainStation>>(stationsWithProduct, HttpStatus.OK);
        return responseEntity;
    }

    @GetMapping("/get/train/{id}")
    @Transactional
    public ResponseEntity<Train> getTrainById(@PathVariable("id") long trainId, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Person person = personRepository.getByUid(userDetails.getUsername());
        Long id = person.getId();

        if (!repository.existsById(id)) {
            ResponseEntity<Train> responseEntity = new ResponseEntity<Train>(HttpStatus.FAILED_DEPENDENCY);
            return responseEntity;
        }

        TrainCompany company = repository.getById(id);

        if(!trainRepository.existsByCompanyId(company.getId())){
            ResponseEntity<Train> responseEntity = new ResponseEntity<Train>(HttpStatus.FAILED_DEPENDENCY);
            return responseEntity;
        }

        if(!company.getTrains().stream().anyMatch(train -> Long.valueOf(trainId).equals(train.getId()))){
            ResponseEntity<Train> responseEntity = new ResponseEntity<Train>(HttpStatus.FORBIDDEN);
            return responseEntity; 
        }
       
        Train train =  trainRepository.getById(trainId);

        ResponseEntity<Train> responseEntity = new ResponseEntity<Train>(train, HttpStatus.OK);
        return responseEntity;
    }

    @Transactional(transactionManager = "")
    private class TrainCompanyBalanceThread extends Thread {
        private Long id;
        private Float deltaBalance;
        private Thread previousThread;

        private TrainCompanyBalanceThread(Long id, Float deltaBalance,Thread previousThread){
            this.id = id;
            this.deltaBalance = deltaBalance; //amount to remove from balance
            this.previousThread = previousThread;
        }

        private void waitForPreviousThread(){
            if(this.previousThread == null){
                return;
            }
            while(previousThread.isAlive()){
                try{
                    Thread.sleep(1000);
                }
                catch(Exception e){
                    System.out.println(e.getMessage());
                }
            }
            return;
        }

        @Override
        public void run(){
            waitForPreviousThread();
            TrainCompany company = repository.getById(id);
            //Float balance = company.getBalance();
            //if(balance<deltaBalance){
                //return;
            //}
            //company.setBalance(company.getBalance()-deltaBalance);
            try {
                repository.save(company);
            } catch (Exception e) {
                // TODO: handle exception
                System.out.println(e.getMessage());
                return;
            }
        }
    }
    
    @Transactional
    private class TrainThread extends Thread {
        private Long  companyId;
        private String trainColor;
        private String trainType;
        private Thread previousThread;
        private boolean success = false; //was saving successful
        private Train train;

        private TrainThread(Long companyId, String trainColor, String trainType ,Thread previousThread){
            this.companyId = companyId;
            this.trainColor = trainColor;
            this.trainType = trainType;
            this.previousThread = previousThread;
        }

        private void waitForPreviousThread(){
            if(this.previousThread == null){
                return;
            }
            while(previousThread.isAlive()){
                try{
                    Thread.sleep(1000);
                }
                catch(Exception e){
                    System.out.println(e.getMessage());
                }
            }
            return;
        }

        @Override
        public void run(){
            waitForPreviousThread();
            TrainCompany company = repository.getById(this.companyId);
            Train train = Train.createTrain();
            train.setTrainColor(this.trainColor);
            train.setTrainType(this.trainType);
            train.setCompany(company);

            try {
                trainRepository.save(train);
            } catch (Exception e) {
                // TODO: handle exception
                System.out.println(e.getMessage());
                return;
            }
            this.success = true;
            this.train = train;
        }

        public boolean getResult(){
            return this.success;
        }

        public Train getTrain(){
            return this.train;
        }
    }

    @Data
    public static class TrainDto {
        private String trainColor;
        private String trainType;
    }

    @PostMapping("/buy/train")
    public ResponseEntity<Train> getTrainById(@RequestBody TrainDto trainDto, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Person person = personRepository.getByUid(userDetails.getUsername());
        Long id = person.getId();

        if (!repository.existsById(id)) {
            ResponseEntity<Train> responseEntity = new ResponseEntity<Train>(HttpStatus.FAILED_DEPENDENCY);
            return responseEntity;
        }

        //build threads
        TrainCompanyBalanceThread trainCompanyBalanceThread = new TrainCompanyBalanceThread(id, Float.valueOf(5000f), null);
        TrainThread trainThread = new TrainThread(id, trainDto.getTrainColor(), trainDto.getTrainType(), trainCompanyBalanceThread);

        //start threads
        trainCompanyBalanceThread.start();
        trainThread.start();

        //wait for completion
        while (trainThread.isAlive()) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                // TODO: handle exception
                System.out.println(e.getMessage());
                ResponseEntity<Train> responseEntity = new ResponseEntity<Train>(HttpStatus.FAILED_DEPENDENCY);
                return responseEntity;
            }  
        }

        //check for success
        if(trainThread.getResult() == false){
            ResponseEntity<Train> responseEntity = new ResponseEntity<Train>(HttpStatus.FAILED_DEPENDENCY);
            return responseEntity;
        }

        //get the resulting train
        Train train = trainThread.getTrain();

        //check if the train is good
        if(train == null){
            ResponseEntity<Train> responseEntity = new ResponseEntity<Train>(HttpStatus.FAILED_DEPENDENCY);
            return responseEntity;
        }

        //finally return the successfully saved train
        ResponseEntity<Train> responseEntity = new ResponseEntity<Train>(train, HttpStatus.OK);
        return responseEntity;
    }
}
