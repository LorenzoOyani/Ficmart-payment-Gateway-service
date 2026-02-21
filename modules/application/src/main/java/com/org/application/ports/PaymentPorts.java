package com.org.application.ports;

import com.org.domain.enums.PaymentStatus;
import com.org.domain.model.Payment;

public interface PaymentPorts {

    Payment authorize(String orderId, String customerId, PaymentStatus paymentStatus);
}
