package com.org.persistence.repository;

import com.org.persistence.entities.TransactionEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TransactionEntity> findByTransactionId(String transactionId);
}
