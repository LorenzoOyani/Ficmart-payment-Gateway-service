package com.org.application.useCase;

import com.org.application.util.Hashing;
import com.org.domain.dto.*;
import com.org.persistence.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@RequiredArgsConstructor
@Service
public class PaymentAuthorizationService {

    private final PaymentRepository paymentRepository;

    private final IdempotencyService idempotencyService;


    public AuthorizeResponse authorizePayment(AuthorizeRequest request, String idempotencyKey) {

        final String endpoint = "http://paymentAuthorization/v1/authorize/payment";

        AuthorizePaymentCommand authorizeCommand = new AuthorizePaymentCommand(
                request.order_id(),
                request.customer_id(),
                request.amount_cents(),
                new IdempotencyKey(idempotencyKey)
        );
        ///  Hash the unchanged part of this request that is required to pass Idempotency request.
        String requestHash = Hashing.sha(
                request.order_id() + "|" + request.customer_id() + "|" + request.amount_cents() + "|USD");

        IdempotencyCommand idempotencyCommand =
                IdempotencyCommand.of(
                        "PAYMENT_AUTHORIZE",
                        requestHash,
                        authorizeCommand.idempotencyKey().toString(),
                        Instant.now()


                );
//        idempotencyService.

        return null;

    }


}
