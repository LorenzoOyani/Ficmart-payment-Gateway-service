package com.org.persistence.mapper;

import com.org.domain.model.Transaction;
import com.org.persistence.entities.TransactionEntity;
import org.mapstruct.Mapper;

@Mapper
public interface TransactionMapper {

    TransactionEntity transactionMappers(Transaction transaction);

    Transaction transactionMapper(TransactionEntity transactionEntity);

}
