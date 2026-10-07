package com.yeom.pass.service.pass;

import com.yeom.pass.repository.pass.BulkPassStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BulkPass {
    private Integer bulkPassSeq;
    private String userGroupId;
    private String userId;
    private Integer count;
    private BulkPassStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

}
