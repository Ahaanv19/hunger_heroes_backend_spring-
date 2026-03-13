package com.open.spring.mvc.stats;

import lombok.Data;

@Data
public class StatsGetDto {
    // Username to get stats for. If null or empty, get all stats.
    private String username;
}
