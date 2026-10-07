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
public class InstructDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String date;

    @OneToMany(mappedBy = "instructDate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InstructDateTime> instructDateTimes = new ArrayList<>();


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instruct_id")
    private Instruct instruct;
}

