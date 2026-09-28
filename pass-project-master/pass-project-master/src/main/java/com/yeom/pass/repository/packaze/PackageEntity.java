
package com.yeom.pass.repository.packaze; // package는 reserved word로 사용할 수 없어서 packaze를 사용합니다.

import com.yeom.pass.repository.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "package")
public class PackageEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 생성을 DB에 위임합니다. (AUTO_INCREMENT)
    private Integer packageSeq;

    private String packageName;
    private Integer count;
    private Integer period;

}

