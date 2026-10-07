package com.yeom.pass.job.pass;


import com.yeom.pass.repository.booking.BookingEntity;
import com.yeom.pass.repository.booking.BookingRepository;
import com.yeom.pass.repository.booking.BookingStatus;
import com.yeom.pass.repository.pass.*;
import com.yeom.pass.repository.user.UserGroupMappingRepository;
import com.yeom.pass.repository.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class AddPassesTasklet implements Tasklet{
    private final PassRepository passRepository;
    private final BulkPassRepository bulkPassRepository;
    private final UserGroupMappingRepository userGroupMappingRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public AddPassesTasklet(PassRepository passRepository, BulkPassRepository bulkPassRepository, UserGroupMappingRepository userGroupMappingRepository
            , BookingRepository bookingRepository, UserRepository userRepository) {
        this.passRepository = passRepository;
        this.bulkPassRepository = bulkPassRepository;
        this.userGroupMappingRepository = userGroupMappingRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }


    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        // 이용권 시작 일시 현재 이후 user group 내 각 사용자에게 이용권을 추가
        final LocalDateTime startedAt = LocalDateTime.now().minusDays(1);
        final List<BulkPassEntity> bulkPassEntities = bulkPassRepository.findByStatusAndStartedAtGreaterThan(BulkPassStatus.READY, startedAt);

        int count = 0;
        int count2 = 0;

        // [개선 1단계]
        //  - 건마다 실행하던 userGroupMappingRepository.findByUserGroupId(...) 제거 (결과를 사용하지 않는 쿼리였음)
        //  - addBooking 이 passRepository.findByUserId 를 반복 호출하던 것을, 방금 저장한 PassEntity 를 직접 넘기는 방식으로 변경
        for(BulkPassEntity bulkPassEntity: bulkPassEntities) {
            PassEntity savedPass = addPass(bulkPassEntity, bulkPassEntity.getUserId());
            count++;

            count2 += addBooking(savedPass);

            bulkPassEntity.setStatus(BulkPassStatus.COMPLETED);
        }

        log.info("AddPassesTasklet - execute: 이용권 {}건 추가 완료, startedAt={}", count, startedAt);
        log.info("AddPassesTasklet - execute: 예약 가능 {}건 추가 완료, startedAt={}", count2, startedAt);
        return RepeatStatus.FINISHED;   // 처리 완료
    }


    // bulkPass 정보로 pass 데이터를 생성하고, 저장된 PassEntity 를 반환
    private PassEntity addPass(BulkPassEntity bulkPassEntity, String userId) {
        PassEntity passEntity = PassModelMapper.INSTANCE.toPassEntity(bulkPassEntity, userId);  // bulkPassEntity -> PassEntity
        return passRepository.save(passEntity);
    }

    // 방금 저장된 pass 에 연결되는 예약 가능(booking) 데이터를 생성
    private int addBooking(PassEntity pass) {
        BookingEntity booking = new BookingEntity();
        booking.setPassSeq(pass.getPassSeq());
        booking.setUserId(pass.getUserId());
        booking.setStatus(BookingStatus.READY);
        booking.setUsedPass(false);
        booking.setAttended(false);
        booking.setStartedAt(null);
        booking.setEndedAt(null);
        booking.setCancelledAt(null);

        bookingRepository.save(booking);
        return 1;
    }
}