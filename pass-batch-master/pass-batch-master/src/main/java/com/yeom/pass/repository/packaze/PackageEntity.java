package com.yeom.pass.repository.packaze;

import com.yeom.pass.repository.BaseEntity;
import com.yeom.pass.repository.pass.PassEntity;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "package")
public class PackageEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer packageSeq;

    private String packageName;
    private Integer count;
    private Integer period;

//    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
//    @JoinColumn(name = "passSeq")    // 양방향 매핑 (지연 로딩 문제)
    //private List<PassEntity> comments = new ArrayList<>();

}
