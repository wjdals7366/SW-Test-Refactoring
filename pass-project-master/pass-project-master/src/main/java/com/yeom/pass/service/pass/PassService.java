package com.yeom.pass.service.pass;

import com.yeom.pass.repository.booking.BookingEntity;
import com.yeom.pass.repository.booking.BookingRepository;
import com.yeom.pass.repository.pass.BulkPassEntity;
import com.yeom.pass.repository.pass.PassEntity;
import com.yeom.pass.repository.pass.PassRepository;
import com.yeom.pass.repository.user.BbsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PassService {
    private final PassRepository passRepository;

    private final BookingRepository bookingRepository;

    public List<Pass> getPasses(final String userId) {
        final List<PassEntity> passEntities = passRepository.findByUserId(userId);
        return PassModelMapper.INSTANCE.map(passEntities);
    }

    public Boolean getBooking(final String userId, final int passSeq){

        BookingEntity bookingEntity = bookingRepository.findByPassSeqAndUserId(passSeq, userId);


        if(bookingEntity == null)
            return false;

        return bookingEntity.isUsedPass();
    }

    public int endBooking(final int bookingSeq){
        int isEnded = bookingRepository.updateBooking(bookingSeq, false);

        return isEnded;
    }
}
