package com.yeom.pass.job.pass;


import com.yeom.pass.repository.booking.BookingEntity;
import com.yeom.pass.repository.booking.BookingRepository;
import com.yeom.pass.repository.booking.BookingStatus;
import com.yeom.pass.repository.pass.*;
import com.yeom.pass.repository.user.UserEntity;
import com.yeom.pass.repository.user.UserGroupMappingEntity;
import com.yeom.pass.repository.user.UserGroupMappingRepository;
import com.yeom.pass.repository.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        // 대량 이용권 정보를 돌면서 user group 에 속한 userId를 조회하고 해당 userId로 이용권을 추가

        for(BulkPassEntity bulkPassEntity: bulkPassEntities) {
            final List<String> userIds = userGroupMappingRepository.findByUserGroupId(bulkPassEntity.getUserGroupId())
                    .stream().map(UserGroupMappingEntity::getUserId).collect(Collectors.toList());  // 해당 그룹의 모든 userid 반환

//            count += addPasses(bulkPassEntity, userIds);
//            count2 += addBooking(userIds);

            count += addPasses(bulkPassEntity, bulkPassEntity.getUserId());
            count2 += addBooking(bulkPassEntity.getUserId());

            bulkPassEntity.setStatus(BulkPassStatus.COMPLETED);

        }

        log.info("AddPassesTasklet - execute: 이용권 {}건 추가 완료, startedAt={}", count, startedAt);
        log.info("AddPassesTasklet - execute: 예약 가능 {}건 추가 완료, startedAt={}", count2, startedAt);
        return RepeatStatus.FINISHED;   // 처리 완료

    }


    // bulkPass 정보로 pass 데이터를 생성
    private int addPasses(BulkPassEntity bulkPassEntity, String userId) {
        List<PassEntity> passEntities = new ArrayList<>();

//        for(String userId: userIds) {
//            PassEntity passEntity = PassModelMapper.INSTANCE.toPassEntity(bulkPassEntity, userId);  // bulkPassEntity -> PassEntity
//            passEntities.add(passEntity);
//        }

        PassEntity passEntity = PassModelMapper.INSTANCE.toPassEntity(bulkPassEntity, userId);  // bulkPassEntity -> PassEntity
        passEntities.add(passEntity);

        return passRepository.saveAll(passEntities).size();
    }

    private int addBooking(String userId){   //List<String> userIds
        List<BookingEntity> bookingEntities = new ArrayList<>();
//        for(String userId:userIds){
//            BookingEntity booking = new BookingEntity();
//            for(int i=0;i<passRepository.findByUserId(userId).size();i++){
//                booking.setPassSeq(passRepository.findByUserId(userId).get(i).getPassSeq());
//            }
//            booking.setUserId(userId);  // 추후 수정
//            booking.setStatus(BookingStatus.READY);
//            booking.setUsedPass(false);
//            booking.setAttended(false);
//            booking.setStartedAt(null);
//            booking.setEndedAt(null);
//            booking.setCancelledAt(null);
//            bookingEntities.add(booking);
//        }

        BookingEntity booking = new BookingEntity();
        for(int i=0;i<passRepository.findByUserId(userId).size();i++){
            booking.setPassSeq(passRepository.findByUserId(userId).get(i).getPassSeq());
        }
        booking.setUserId(userId);  // 추후 수정
        booking.setStatus(BookingStatus.READY);
        booking.setUsedPass(false);
        booking.setAttended(false);
        booking.setStartedAt(null);
        booking.setEndedAt(null);
        booking.setCancelledAt(null);
        bookingEntities.add(booking);

        return bookingRepository.saveAll(bookingEntities).size();
    }
}
