package com.org.application.mapper;

import com.org.persistence.entities.Payment;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface PaymentMapper {

    Payment toPaymentEntity(com.org.domain.model.Payment payment);

    com.org.domain.model.Payment toPayment(Payment payment);
}
