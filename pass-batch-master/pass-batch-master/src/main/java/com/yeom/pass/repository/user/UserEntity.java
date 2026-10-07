package com.yeom.pass.repository.user;

import com.vladmihalcea.hibernate.type.json.JsonType;
import com.yeom.pass.repository.BaseEntity;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.Map;

@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "user")
// json의 타입을 정의
@TypeDef(name = "json", typeClass = JsonType.class)
//@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity{

    @Id
    private String userId;

    private String userName;
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    private String phone;

    // json 형태로 저장되어 있는 문자열 데이터를 Map 으로 매핑
    @Type(type = "json")
    @Column(name = "meta", columnDefinition = "text")   // meta 타입을 text 로 지정
    private Map<String, Object> meta;

    public String getUuid() {
        String uuid = null;
        if (meta.containsKey("uuid")) {
            uuid = String.valueOf(meta.get("uuid"));
        }
        return uuid;

    }

}