package com.example.bankservice.application.mapper;

import com.example.bankservice.application.dto.BankDto;
import com.example.bankservice.domain.model.Bank;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BankAppMapper {

    BankDto toDto(Bank bank);

    Bank toDomain(BankDto dto);
}
