package com.example.bankservice.infrastructure.web;

import com.example.bankservice.application.dto.BankDto;
import com.example.bankservice.domain.model.Bank;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BankApiMapper {

    // Request -> App DTO
    BankDto toDto(BankRequest request);

    // App DTO -> Response
    BankResponse toResponse(BankDto dto);
}
