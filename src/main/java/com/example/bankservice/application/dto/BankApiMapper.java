package com.example.bankservice.application.dto;

import com.example.bankservice.domain.model.Bank;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BankApiMapper {

    // Domain -> API
    BankResponse toResponse(Bank bank);

}
