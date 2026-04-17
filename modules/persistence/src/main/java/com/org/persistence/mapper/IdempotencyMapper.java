package com.org.persistence.mapper;

import com.org.domain.model.IdempotencyRecord;
import com.org.persistence.entities.IdempotencyEntity;
import org.mapstruct.Mapper;

@Mapper
public interface IdempotencyMapper  {

    IdempotencyRecord IdempotencyRecordMapper(IdempotencyEntity idempotencyEntity);

    IdempotencyEntity  IdempotencyEntityMapper(IdempotencyRecord idempotencyRecord);
}
