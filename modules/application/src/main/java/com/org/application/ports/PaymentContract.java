package com.org.application.ports;

import com.org.domain.dto.PaymentRequest;
import com.org.domain.enums.PaymentStatus;
import com.org.domain.model.Payment;
import com.org.domain.model.Receipts;
import com.org.persistence.entities.PaymentEntity;

public interface PaymentContract {

    Receipts createPayment(PaymentRequest paymentRequest);
    void updatePayment(Payment payment, PaymentStatus paymentStatus);


}
