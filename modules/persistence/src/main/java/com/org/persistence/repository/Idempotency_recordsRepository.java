package com.org.persistence.repository;

import com.org.persistence.entities.Idempotency_records;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface Idempotency_recordsRepository extends JpaRepository<Idempotency_records, UUID> {

    Optional<Idempotency_records> findByMerchantIdAndOperationAndKey(
            String merchantId,
            String operation,
            String idemKey
    );

}
