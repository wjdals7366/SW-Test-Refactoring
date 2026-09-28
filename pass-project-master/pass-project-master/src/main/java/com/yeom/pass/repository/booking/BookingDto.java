package com.yeom.pass.repository.booking;

import com.yeom.pass.repository.instructor.Instruct;
import com.yeom.pass.repository.pass.PassEntity;
import com.yeom.pass.repository.user.UserEntity;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {

    private String date;

    private Integer bookingSeq;
    private String userName;
    private String userId;

    private String startedAt;
    private String endedAt;

    private String instructorName;
}
