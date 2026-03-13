package com.open.spring.mvc.stats;

import lombok.Data;

// This DTO will be used for the @RequestBody of DELETE requests
@Data
public class StatsDeleteDto {
    private String username;
    private String module;
    private Integer submodule;
}
