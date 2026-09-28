package com.yeom.pass.repository.pass;

import com.yeom.pass.repository.user.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

// Setter 를 직접 구현하지 않고 사용하기 위해
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)  // ReportingPolicy.IGNORE: 일치하지 않은 필드를 무시
public interface PassModelMapper {
    PassModelMapper INSTANCE = Mappers.getMapper(PassModelMapper.class);

    // 필드명이 같지 않거나 custom 하게 매핑해주기 위해 @Mapping 을 추가
    @Mapping(target = "status", qualifiedByName = "defaultStatus")
    @Mapping(target = "remainingCount", source = "bulkPassEntity.count")
    PassEntity toPassEntity(BulkPassEntity bulkPassEntity, String userId);  // userId는 bulkPassEntity 에 존재하지 않는 PassEntity 필드

    // BulkPassStatus 와 관계 없이 PassEntity 의 PassStatus 값을 설정
    @Named("defaultStatus")
    default PassStatus status(BulkPassStatus status) {
        return PassStatus.READY;
    }

}
