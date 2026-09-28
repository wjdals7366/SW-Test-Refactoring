package com.yeom.pass.repository.pass;

import com.yeom.pass.repository.BaseEntity;
import com.yeom.pass.repository.packaze.PackageEntity;
import com.yeom.pass.repository.user.UserEntity;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "pass")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 생성을 DB에 위임 (AUTO_INCREMENT)
    private Integer passSeq;

    private Integer packageSeq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private UserEntity user;

    private String userId;

    @Enumerated(EnumType.STRING)
    private PassStatus status;
    private Integer remainingCount;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private LocalDateTime expiredAt;

}
