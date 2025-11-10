package com.example.bankservice.infrastructure.repository.mapper;

import com.example.bankservice.domain.model.Bank;
import com.example.bankservice.infrastructure.repository.entity.BankJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface BankEntityMapper {

    BankEntityMapper INSTANCE = Mappers.getMapper(BankEntityMapper.class);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "bic", source = "bic")
    @Mapping(target = "country", source = "country")
    @Mapping(target = "routingNumber", source = "routingNumber")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    BankJpaEntity toEntity(Bank domain);

    Bank toDomain(BankJpaEntity entity);
}
