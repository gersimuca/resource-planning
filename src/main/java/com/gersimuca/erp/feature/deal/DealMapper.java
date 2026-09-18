package com.gersimuca.erp.feature.deal;

import com.gersimuca.erp.common.MapperConfig;
import com.gersimuca.erp.feature.user.JwtMapper;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class, uses = JwtMapper.class)
public interface DealMapper {
  List<DealDto> toDtoList(List<DealEntity> entities);

  @Mapping(source = "ownerId.userId", target = "ownerId")
  @Mapping(source = "customerId.customerId", target = "customerId")
  DealDto toDto(DealEntity entity);

  @Mapping(target = "ownerId", ignore = true)
  @Mapping(target = "customerId", ignore = true)
  DealEntity toEntity(DealDto dealDto);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "lastUpdatedAt", ignore = true)
  @Mapping(target = "lastModifiedBy", ignore = true)
  @Mapping(target = "ownerId", ignore = true)
  @Mapping(target = "customerId", ignore = true)
  void copyToEntity(DealDto dto, @MappingTarget DealEntity entity);
}
