package com.yeom.pass.service.pass;

import com.yeom.pass.repository.pass.PassStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Pass {
    private Integer passSeq;
    private Integer packageSeq;
    private String packageName;
    private String userId;

    private PassStatus status;
    private Integer remainingCount;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private LocalDateTime expiredAt;

}
