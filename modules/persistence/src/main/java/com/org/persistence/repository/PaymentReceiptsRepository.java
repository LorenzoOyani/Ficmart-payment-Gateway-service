package com.org.persistence.repository;

import com.org.persistence.models.PaymentReceipts;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentReceiptsRepository extends JpaRepository<PaymentReceipts, String> {
    Optional<PaymentReceipts> findByOrderId(String orderId);

    List<PaymentReceipts> findByCustomerIdOrderByCreatedAtDesc(String customerId);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM payment_receipts WHERE p.paymentRef = :paymentRef")
    Optional<PaymentReceipts> findByPaymentRef(@Param("param") Long paymentRef);
}
