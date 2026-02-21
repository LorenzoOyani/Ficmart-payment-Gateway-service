package com.org.infrastructure.bank;

import com.org.persistence.entities.BankAuthorization;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankAuthorizationRepository extends JpaRepository<BankAuthorization, Long> {

    Optional<BankAuthorization> findByOrderIdAndCustomerId(String orderId, String customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM BankAuthorization a WHERE a.authId= :authId")
    Optional<BankAuthorization> findByAuthId(String authId);
}
