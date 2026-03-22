package com.org.application.usecase;

import com.org.application.ports.PaymentProvider;
import com.org.application.dto.AuthorizeResponse;
import com.org.application.util.Hashing;
import com.org.domain.dto.*;
import com.org.domain.exception.IdempotencyIdentityConflictException;
import com.org.domain.model.IdempotencyState;
import com.org.infrastructure.service.IdempotencyService;
import com.org.persistence.entities.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

@RequiredArgsConstructor
@Service
public class GatewayAuthorizationService {

    private static final String operation = "PAYMENT_AUTHORIZE";
    private static final String CURRENCY = "USD";
    private static final String ENDPOINT = "http://paymentAuthorization/v1/authorize/payment";


    private final IdempotencyContract idempotencyContract;
    private final PaymentProvider paymentProvider;
    private final TransactionTemplate transactionTemplate;
    private final IdempotencyService<?> idempotencyService;


    public AuthorizeResponse authorizePayment(AuthorizeRequest request, String idempotencyKey) throws IdempotencyIdentityConflictException {

        final String merchantId = "x-merchantId";

        final String requestHash = Hashing.sha(
                request.order_id() + "|" +
                        request.customer_id() + "|" +
                        request.amount_cents() + "|" +
                        CURRENCY + "|" +
                        ENDPOINT

        );

        IdempotencyCommand idempotencyCommand = IdempotencyCommand.of(
                merchantId,
                requestHash,
                operation,
                Instant.now(),
                idempotencyKey
        );

        try{
        IdempotencyState<AuthorizeResponse> idempotencyState = idempotencyService.begin(idempotencyCommand, AuthorizeResponse.class);

        ///  this check ensures system consistency under a potential payment duplication
        if(idempotencyState.isReplay()){ ///  to get a replay, the transaction must either be... Completed or a total failure
           return idempotencyState.getPaymentResponse();
        }

        if(idempotencyState.isInProgress()){
            return new AuthorizeResponse(
                    "X-providerId",
                    "...in progress",
                    "Another authorization request with this idempotency key is already in progress"
            );


            Payment payment = transactionTemplate.execute(status -> {
                Payment payments = Payment
            }
        })



            )
        }

    }
}