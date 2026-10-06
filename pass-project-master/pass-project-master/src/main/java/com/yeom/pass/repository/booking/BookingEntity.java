package com.yeom.pass.repository.booking;


import com.yeom.pass.repository.BaseEntity;
import com.yeom.pass.repository.instructor.Instruct;
import com.yeom.pass.repository.pass.PassEntity;
import com.yeom.pass.repository.user.UserEntity;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@ToString
@Entity
@Table(name = "booking")
@NoArgsConstructor
@AllArgsConstructor
public class BookingEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bookingSeq;
    private Integer passSeq;
    //private String userId;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;
    private boolean usedPass;
    private boolean attended;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private LocalDateTime cancelledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",insertable = false, updatable = false)
    private UserEntity userEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passSeq", insertable = false, updatable = false)
    private PassEntity passEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructId", insertable = false, updatable = false)
    private Instruct instruct;

    private String instructorName;


    // endedAt 기준, yyyy-MM-HH 00:00:00  -> 일 단위로 변환
    public LocalDateTime getStatisticsAt() {
        return this.endedAt.withHour(0).withMinute(0).withSecond(0).withNano(0);

    }
}
