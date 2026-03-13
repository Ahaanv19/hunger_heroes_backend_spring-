package com.open.spring.mvc.stats;

import lombok.Data;

@Data
public class StatsUpdateDto {
    // We add username here, so it's part of the JSON body
    private String username;
    
    // Module/submodule uniquely identify the stat entry to update
    private String module;
    private Integer submodule;

    // Fields that can be updated
    private Boolean finished;
    private Double time;
    private Double grades;
}
