package com.yeom.pass.repository.user;

import com.yeom.pass.repository.BaseEntity;
import com.yeom.pass.service.user.User;
import lombok.*;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Comment extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne  // 해당 댓글 엔티티 여러개가, 하나의 Article에 연관
    @JoinColumn(name = "bbs_id") // "엔티티 객체 자체를 통째로 참조 = FK -> 필드명 +  “_” + 참조하는 테이블의 기본 키(@Id) 컬럼명
    private Bbs bbs;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    private String nickname;

    private String body;

    private String Datetime;    // 'T' 제거를 위한 필드

}
