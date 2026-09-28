package com.yeom.pass.repository.instructor;

import lombok.*;

import javax.persistence.*;
import java.util.Map;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class InstructDto {

    private Integer id;

    private String name;

    private String time;

    private String limitNumber;

    private String reserveNumber;
}
