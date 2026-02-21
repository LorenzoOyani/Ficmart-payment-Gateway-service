package com.org.persistence.mapper;

import com.org.domain.model.Account;
import com.org.persistence.entities.AccountEntity;
import org.mapstruct.Mapper;

@Mapper
public interface AccountMapper {

    Account AccountEntityMapper(AccountEntity account);
}
