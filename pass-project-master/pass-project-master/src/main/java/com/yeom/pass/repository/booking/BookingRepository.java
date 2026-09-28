package com.yeom.pass.repository.booking;

import com.yeom.pass.repository.booking.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface BookingRepository extends JpaRepository<BookingEntity, Integer> {
    @Transactional
    @Modifying
    @Query(value = "UPDATE BookingEntity b" +
            "          SET b.usedPass = :usedPass," +
            "              b.modifiedAt = CURRENT_TIMESTAMP" +
            "        WHERE b.passSeq = :passSeq")
    int updateUsedPass(Integer passSeq, boolean usedPass);

    @Query(value = "SELECT b FROM BookingEntity b WHERE b.passEntity.passSeq = :passSeq AND b.userEntity.userId = :userId AND b.status = 'READY'")
    BookingEntity findByPassSeqAndUserId(int passSeq, String userId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE BookingEntity b SET b.usedPass = :usedPass WHERE b.bookingSeq = :bookingSeq")
    int updateBooking(int bookingSeq, boolean usedPass);

}
