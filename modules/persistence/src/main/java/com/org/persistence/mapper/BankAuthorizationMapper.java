package com.org.persistence.mapper;

import com.org.persistence.entities.BankAuthorization;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper
public interface BankAuthorizationMapper {

    BankAuthorization bankAuthMapper(com.org.domain.model.BankAuthorization bankAuthorization);
}
