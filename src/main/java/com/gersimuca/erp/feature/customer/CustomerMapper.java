package com.gersimuca.erp.feature.customer;

import com.gersimuca.erp.common.MapperConfig;
import com.gersimuca.erp.feature.user.JwtMapper;
import com.gersimuca.erp.model.CustomerModel;
import com.gersimuca.erp.model.CustomerStatus;
import com.gersimuca.erp.model.CustomersPageModel;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;

/**
 * @author gersimuca
 */
@Mapper(config = MapperConfig.class, uses = JwtMapper.class)
public interface CustomerMapper {
  Status toStatus(CustomerStatus status);

  @Mapping(source = "ownerId.userId", target = "ownerId")
  CustomerDto toDto(final CustomerEntity entity);

  List<CustomerDto> toDtoList(final List<CustomerEntity> entities);

  @Mapping(target = "metadataDto", source = ".")
  @Mapping(target = "customerDtoList", source = "content")
  CustomersPageDto toPageDto(Page<CustomerEntity> page);

  @Mapping(target = "totalElements", source = "totalElements")
  @Mapping(target = "totalPages", source = "totalPages")
  @Mapping(target = "size", source = "size")
  @Mapping(target = "number", source = "number")
  @Mapping(target = "first", source = "first")
  @Mapping(target = "last", source = "last")
  CustomerPageMetadataDto toMetadataDto(Page<CustomerEntity> page);

  @Mapping(target = "ownerId", ignore = true)
  @Mapping(target = "customerId", ignore = true)
  @Mapping(target = "deals", ignore = true)
  CustomerEntity toEntity(final CustomerDto dto);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "lastUpdatedAt", ignore = true)
  @Mapping(target = "lastModifiedBy", ignore = true)
  @Mapping(target = "ownerId", ignore = true)
  @Mapping(target = "deals", ignore = true)
  void copyToEntity(CustomerDto dto, @MappingTarget CustomerEntity entity);

  @Mapping(target = "customers", source = "customerDtoList")
  @Mapping(target = "totalElements", source = "metadataDto.totalElements")
  @Mapping(target = "totalPages", source = "metadataDto.totalPages")
  @Mapping(target = "size", source = "metadataDto.size")
  @Mapping(target = "number", source = "metadataDto.number")
  @Mapping(target = "first", source = "metadataDto.first")
  @Mapping(target = "last", source = "metadataDto.last")
  CustomersPageModel toResponse(CustomersPageDto dto);

  CustomerModel toModel(CustomerDto dto);

  List<CustomerModel> toModelList(List<CustomerDto> dtoList);

  CustomerDto toDto(CustomerModel model);
}
