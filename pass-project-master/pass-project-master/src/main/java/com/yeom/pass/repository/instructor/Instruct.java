package com.yeom.pass.repository.instructor;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Instruct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @OneToMany(mappedBy = "instruct", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InstructDate> instructDates = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Gender gender;

}
