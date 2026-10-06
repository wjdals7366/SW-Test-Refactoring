package com.yeom.pass.controller.admin;

import com.yeom.pass.util.LocalDateTimeUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class BulkPassRequest {
    private Integer packageSeq;
//    private String userGroupId;
    private String userId;
    private LocalDateTime startedAt;

    public void setStartedAt(String startedAtString) {
        this.startedAt = LocalDateTimeUtils.parse(startedAtString);

    }

}
