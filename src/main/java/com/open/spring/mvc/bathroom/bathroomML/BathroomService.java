package com.open.spring.mvc.bathroom.bathroomML;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.open.spring.mvc.bathroom.Tinkle;
import com.open.spring.mvc.bathroom.TinkleJPARepository;

@Service
public class BathroomService {
    @Autowired
    private TinkleJPARepository tinkleJPARepository;

    public List<Tinkle> getAllLogs() {
        return tinkleJPARepository.findAll();
    }
}
