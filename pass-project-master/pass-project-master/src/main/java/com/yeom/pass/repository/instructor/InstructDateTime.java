package com.yeom.pass.repository.instructor;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InstructDateTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instruct_date_id")
    private InstructDate instructDate;

    private String time;

    private Integer limitNumber;
    private Integer reserveNumber;
}
