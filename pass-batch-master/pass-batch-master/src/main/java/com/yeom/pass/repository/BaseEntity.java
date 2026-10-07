package com.yeom.pass.repository;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {
    @CreatedDate // 생성 일시를 생성합니다.
    @Column(columnDefinition = "datetime(0) default now()", updatable = false, nullable = false) // 업데이트를 하지 않도록, null이 되지 않도록 명시합니다.
    private LocalDateTime createdAt;
    @LastModifiedDate // 마지막 수정 일시를 생성합니다.
    private LocalDateTime modifiedAt;
}
