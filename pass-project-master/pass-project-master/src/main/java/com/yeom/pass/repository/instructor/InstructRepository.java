package com.yeom.pass.repository.instructor;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface InstructRepository extends JpaRepository<Instruct, Integer> {
    List<InstructDate> findInstructDatesById(int instructId);
    List<InstructDateTime> findInstructDateTimesByInstructDatesId(int instructDateId);
}
