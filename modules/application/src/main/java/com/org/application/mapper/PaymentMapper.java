package com.org.application.mapper;

import com.org.domain.model.Payment;
import com.org.persistence.entities.PaymentEntity;
import org.mapstruct.Mapper;

@Mapper
public interface PaymentMapper {

    PaymentEntity toPaymentEntity(Payment payment);

    Payment toPayment(PaymentEntity paymentEntity);
}
