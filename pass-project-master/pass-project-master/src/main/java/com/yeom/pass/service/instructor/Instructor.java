package com.yeom.pass.service.instructor;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Instructor {
    private String name;
    private List<String> timeList;

}
