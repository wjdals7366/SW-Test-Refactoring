package com.yeom.pass.repository.Instructor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

public interface InstructRepository extends JpaRepository<Instruct, Integer> {

    @Transactional
    @Modifying
    @Query(value = "UPDATE InstructDateTime dt" +
            "          SET dt.reserveNumber = :reserveNumber" +
            "        WHERE dt.id = :id")
    int updateReserve(Integer id, Integer reserveNumber);
}
