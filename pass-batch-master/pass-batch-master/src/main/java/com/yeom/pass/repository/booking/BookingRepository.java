package com.yeom.pass.repository.booking;
import com.yeom.pass.repository.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

public interface BookingRepository extends JpaRepository<BookingEntity, Integer> {
    @Transactional
    @Modifying
    @Query(value = "UPDATE BookingEntity b" +
            "          SET b.usedPass = :usedPass," +
            "              b.modifiedAt = CURRENT_TIMESTAMP," + "b.startedAt = :startedAt," + "b.endedAt = :endedAt," + "b.instructorName = :instructorName" +
            "        WHERE b.passSeq = :passSeq")
    int updateUsedPass(Integer passSeq, boolean usedPass, LocalDateTime startedAt, LocalDateTime endedAt, String instructorName);

    @Query(value = "SELECT b FROM BookingEntity b WHERE b.passEntity.passSeq = :passSeq AND b.userId = :userId AND b.status = 'READY'")
    BookingEntity findByPassSeqAndUserId(int passSeq, String userId);
}
