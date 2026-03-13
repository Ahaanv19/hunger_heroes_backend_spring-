package com.open.spring.mvc.stats;

import lombok.Data;

@Data
public class StatsGradeDto {
    private String username;
    private String module;
    private Integer submodule;
    private String question;
    private String response;
}
