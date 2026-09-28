package com.yeom.pass.job.pass;

import com.yeom.pass.repository.pass.*;
import com.yeom.pass.repository.user.UserGroupMappingEntity;
import com.yeom.pass.repository.user.UserGroupMappingRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class) // 테스트 클래스가 Mockito 를 사용함을 의미
public class AddPassesTaskletTest {
    @Mock
    private StepContribution stepContribution;

    @Mock
    private ChunkContext chunkContext;

    @Mock
    private PassRepository passRepository;

    @Mock
    private BulkPassRepository bulkPassRepository;

    @Mock
    private UserGroupMappingRepository userGroupMappingRepository;

    // @InjectMocks 클래스의 인스턴스를 생성하고 @Mock 으로 생성된 객체를 주입 (위와 같은 Mock 객체가 주입된 클래스를 사용)
    @InjectMocks
    private AddPassesTasklet addPassesTasklet;

    @Captor
    ArgumentCaptor<List<PassEntity>> listArgumentCaptor;

    @Test
    public void test_execute() {
        // given : 테스트를 위한 준비 과정
        final String userGroupId = "GROUP";
        final String userId = "A1000000";
        final Integer packageSeq = 1;
        final Integer count = 10;

        final LocalDateTime now = LocalDateTime.now();

        final BulkPassEntity bulkPassEntity = new BulkPassEntity();
        bulkPassEntity.setPackageSeq(packageSeq);
        bulkPassEntity.setUserGroupId(userGroupId);
        bulkPassEntity.setStatus(BulkPassStatus.READY);
        bulkPassEntity.setCount(count);
        bulkPassEntity.setStartedAt(now);
        bulkPassEntity.setEndedAt(now.plusDays(60));

        final UserGroupMappingEntity userGroupMappingEntity = new UserGroupMappingEntity();
        userGroupMappingEntity.setUserGroupId(userGroupId);
        userGroupMappingEntity.setUserId(userId);

        // when : 테스트를 실행하는 과정
        when(bulkPassRepository.findByStatusAndStartedAtGreaterThan(eq(BulkPassStatus.READY), any())).thenReturn(List.of(bulkPassEntity));
        when(userGroupMappingRepository.findByUserGroupId(eq("GROUP"))).thenReturn(List.of(userGroupMappingEntity));
        // when().thenReturn()을 이용하여 해당 Repository 의 메소드를 호출하게 되면 즉시 임의로 설정한 Entity 를 반환하도록 만듬

        RepeatStatus repeatStatus = addPassesTasklet.execute(stepContribution, chunkContext);

        // then : 테스트를 검증하는 과정
        // execute 의 return 값인 RepeatStatus 값을 확인
        assertEquals(RepeatStatus.FINISHED, repeatStatus);

        // 추가된 PassEntity 값을 확인
        ArgumentCaptor<List> passEntitiesCaptor = ArgumentCaptor.forClass(List.class);  // ArgumentCaptor : 메소드에 들어가는 인자값 검증
        verify(passRepository, times(1)).saveAll(passEntitiesCaptor.capture()); // passRepository.saveAll 의 인자값 검증
        // #verify($Target 되는 인스턴스).$Target 메서드(정의된ArgumentCaptor.capture());
        final List<PassEntity> passEntities = passEntitiesCaptor.getValue();    // 메서드에 전달된 인자를 값으로 할당

        assertEquals(1, passEntities.size());

        final PassEntity passEntity = passEntities.get(0);
        assertEquals(packageSeq, passEntity.getPackageSeq());
        assertEquals(userId, passEntity.getUserId());
        assertEquals(PassStatus.READY, passEntity.getStatus());
        assertEquals(count, passEntity.getRemainingCount());

    }
}
