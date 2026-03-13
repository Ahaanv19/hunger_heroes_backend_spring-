package com.open.spring.mvc.groups;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupChatMessage {
    private String name;
    private String message;
    private String date;
    private String image;
}
