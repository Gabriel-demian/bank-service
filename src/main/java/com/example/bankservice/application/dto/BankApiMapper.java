package com.example.bankservice.application.dto;

import com.example.bankservice.domain.model.Bank;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BankApiMapper {

    // Domain -> API
    BankResponse toResponse(Bank bank);

    List<BankResponse> toResponseList(List<Bank> banks);

}
