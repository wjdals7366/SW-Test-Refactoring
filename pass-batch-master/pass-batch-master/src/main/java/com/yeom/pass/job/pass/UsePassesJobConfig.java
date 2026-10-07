package com.yeom.pass.job.pass;
import com.yeom.pass.repository.Instructor.Instruct;
import com.yeom.pass.repository.Instructor.InstructDateTime;
import com.yeom.pass.repository.Instructor.InstructRepository;
import com.yeom.pass.repository.booking.BookingEntity;
import com.yeom.pass.repository.booking.BookingRepository;
import com.yeom.pass.repository.booking.BookingStatus;
import com.yeom.pass.repository.pass.PassEntity;
import com.yeom.pass.repository.pass.PassRepository;
import com.yeom.pass.repository.statistics.StatisticsEntity;
import com.yeom.pass.util.LocalDateTimeUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.integration.support.json.JsonOutboundMessageMapper;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.Future;

@Configuration
public class UsePassesJobConfig {   // 수업 종료 후 이용권 차감

    private final int CHUNK_SIZE = 10;

    // @EnableBatchProcessing로 인해 Bean으로 제공된 JobBuilderFactory, StepBuilderFactory
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private final PassRepository passRepository;
    private final BookingRepository bookingRepository;
    private final InstructRepository instructRepository;

    public UsePassesJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, EntityManagerFactory entityManagerFactory,
                              PassRepository passRepository, BookingRepository bookingRepository, InstructRepository instructRepository) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory;
        this.passRepository = passRepository;
        this.bookingRepository = bookingRepository;
        this.instructRepository = instructRepository;
    }

    @Bean
    public Job usePassesJob() {
        return this.jobBuilderFactory.get("usePassesJob")
                //.incrementer(new RunIdIncrementer())
                .start(addReserveStep())
                .next(usePassesStep()) // 예약 인원 증가
                .build();

    }

    @Bean
    public Step usePassesStep() {
        return this.stepBuilderFactory.get("usePassesStep")
                .<BookingEntity, Future<BookingEntity>>chunk(CHUNK_SIZE)
                .reader(usePassesItemReader(null, 0))
                .processor(usePassesAsyncItemProcessor())
                .writer(usePassesAsyncItemWriter())
                .allowStartIfComplete(true)// 완료된 스텝 재실행하기
                .build();

    }

//    @Bean
//    private Step cancelPassStep() {
//        return this.stepBuilderFactory.get("cancelPassStep")
//                .<BookingEntity, BookingEntity>chunk(CHUNK_SIZE)
//                .reader()
//                .processor()
//                .writer()
//                .build();
//    }

    @Bean
    @StepScope
    public JpaCursorItemReader<BookingEntity> usePassesItemReader(@Value("#{jobParameters[userId]}") String userId, @Value("#{jobParameters[passSeq]}") int passSeq)
    {

        return new JpaCursorItemReaderBuilder<BookingEntity>()
                .name("usePassesItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select b from BookingEntity b join fetch b.passEntity where b.status = :status and b.usedPass = false and b.userId = :userId and b.passSeq = :passSeq")
                // usedPass = false 로 예약 두번 이상 불가
                .parameterValues(Map.of("status", BookingStatus.READY, "userId", userId, "passSeq", passSeq)) // "endedAt", LocalDateTime.now(),
                .build();   // join fetch : 엔티티들 간의 관계를 맺고 있는 경우 지연 로딩(lazy loading) 대신에 즉시 로딩(eager loading)을 수행
    }

    @Bean
    public AsyncItemProcessor<BookingEntity, BookingEntity> usePassesAsyncItemProcessor(){  // ItemProcessor 에 병목현상이 있는 경우 성능 up
        AsyncItemProcessor<BookingEntity, BookingEntity> asyncItemProcessor = new AsyncItemProcessor<>();
        asyncItemProcessor.setDelegate(usePassesItemProcessor(null, null, null));   // usePassesItemProcessor 가 새로운 Thread 위에서 동작 = 멀티쓰레드로 동작
        asyncItemProcessor.setTaskExecutor(new SimpleAsyncTaskExecutor());  // Thread 를 생성하여 processor 에 실제 작업 위임
        return asyncItemProcessor;
    } // -> ItemProcessor 에게 실행을 위임하고 결과를 Future 에 저장

    @Bean
    @StepScope
    public ItemProcessor<BookingEntity, BookingEntity> usePassesItemProcessor(@Value("#{jobParameters[started_at]}") String started_at,
                                                                              @Value("#{jobParameters[ended_at]}") String ended_at, @Value("#{jobParameters[instructor_name]}") String instructor_name) {
        return bookingEntity -> {

            PassEntity passEntity = bookingEntity.getPassEntity();
            passEntity.setRemainingCount(passEntity.getRemainingCount() - 1);
            bookingEntity.setPassEntity(passEntity);

            bookingEntity.setStartedAt(LocalDateTimeUtils.parse(started_at));
            bookingEntity.setEndedAt(LocalDateTimeUtils.parse(ended_at));
            bookingEntity.setInstructorName(instructor_name);

            bookingEntity.setUsedPass(true);    // 예약으로 인한 이용권 1회 소진
            return bookingEntity;
        };
    }

    @Bean
    public AsyncItemWriter<BookingEntity> usePassesAsyncItemWriter() {  // AsyncItemWriter : 실행 결과 값들이 넘어올때마다 개별적으로 처리 -> 비동기 처리 (병렬 실행)
        AsyncItemWriter<BookingEntity> asyncItemWriter = new AsyncItemWriter<>();
        asyncItemWriter.setDelegate(usePassesItemWriter());
        return asyncItemWriter;

    }

    @Bean
    public ItemWriter<BookingEntity> usePassesItemWriter(){
        return bookEntities -> {
            for(BookingEntity bookingEntity : bookEntities){
                int updatedCount = passRepository.updateRemainingCount(bookingEntity.getPassSeq(), bookingEntity.getPassEntity().getRemainingCount());
                if(updatedCount > 0) {
                    bookingRepository.updateUsedPass(bookingEntity.getPassSeq(), bookingEntity.isUsedPass(), bookingEntity.getStartedAt(),
                            bookingEntity.getEndedAt(), bookingEntity.getInstructorName());
                }
            }
        };
    }


    @Bean
    public Step addReserveStep() {
        return this.stepBuilderFactory.get("AddReserveStep")
                .<InstructDateTime, InstructDateTime>chunk(CHUNK_SIZE)
                .reader(addReserveItemReader(0))
                .processor(addReserveProcessor(null, 0))
                .writer(addReserveItemWriter())
                .allowStartIfComplete(true)// 완료된 스텝 재실행하기
                .build();

    }

    @Bean
    @StepScope
    public JpaCursorItemReader<InstructDateTime> addReserveItemReader(@Value("#{jobParameters[instructor_id]}") int instructor_date_time_id)
    {
        return new JpaCursorItemReaderBuilder<InstructDateTime>()
                .name("usePassesItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT dt FROM InstructDateTime dt JOIN dt.instructDate d JOIN d.instruct i where dt.id = :id")   // 특정 시간에 예약된 id 가져옴
                .parameterValues(Map.of("id", instructor_date_time_id))
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<InstructDateTime, InstructDateTime> addReserveProcessor(@Value("#{jobParameters[userId]}") String userId, @Value("#{jobParameters[passSeq]}") int passSeq){  // ItemProcessor 에 병목현상이 있는 경우 성능 up
        return instructDateTime -> {

            BookingEntity bookingEntity = bookingRepository.findByPassSeqAndUserId(passSeq, userId);
            if (bookingEntity != null && bookingEntity.isUsedPass()) {  // 이미 예약한 대상은 예약해도 인원 증가x
                return instructDateTime;
            }
            instructDateTime.setReserveNumber(instructDateTime.getReserveNumber() + 1);
            return instructDateTime;
        };
    }
    @Bean
    public ItemWriter<InstructDateTime> addReserveItemWriter(){
        return instructDateTime -> {
            for(InstructDateTime i : instructDateTime){
                instructRepository.updateReserve(i.getId(), i.getReserveNumber());
            }
        };
    }
}
