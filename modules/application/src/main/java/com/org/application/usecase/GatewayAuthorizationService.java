package com.org.application.usecase;

import com.org.application.dto.PaymentProviderAuthorizeCmd;
import com.org.application.mapper.PaymentMapper;
import com.org.application.ports.PaymentProvider;
import com.org.application.dto.AuthorizeResponse;
import com.org.application.util.Hashing;
import com.org.application.util.Retry;
import com.org.domain.dto.*;
import com.org.domain.exception.IdempotencyIdentityConflictException;
import com.org.domain.model.IdempotencyState;
import com.org.domain.model.Payment;
import com.org.infrastructure.service.IdempotencyService;
import com.org.persistence.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class GatewayAuthorizationService {

    private static final String operation = "PAYMENT_AUTHORIZE";
    private static final String CURRENCY = "USD";
    private static final String ENDPOINT = "http://paymentAuthorization/v1/authorize/payment";


    private final PaymentProvider paymentProvider;
    private final TransactionTemplate transactionTemplate;
    private final IdempotencyService<?> idempotencyService;
    private final PaymentMapper paymentMapper;
    private final PaymentRepository paymentRepository;


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

            IdempotencyState<AuthorizeResponse> idempotencyState = idempotencyService.begin(idempotencyCommand, AuthorizeResponse.class);

            ///  this check ensures system consistency under a potential payment de-duplication
            if (idempotencyState.isReplay()) { ///  to get a replay, the transaction must either be... Completed or a total failure
                return idempotencyState.getPaymentResponse();
            }

            if (idempotencyState.isInProgress()) {
                return new AuthorizeResponse(
                        "X-providerId",
                        "...in progress",
                        "Another authorization request with this idempotency key is already in progress"
                );
            }
                com.org.persistence.entities.Payment payment = transactionTemplate.execute(status -> {
                    Payment payments = Payment.newPending(

                            request.customer_id(),
                            request.order_id(),
                            request.amount_cents(),
                            request.currency(),
                            merchantId
                    );
                    com.org.persistence.entities.Payment paymentDb = paymentMapper.toPaymentEntity(payments);


                    return paymentRepository.save(paymentDb);
                });

            PaymentAuthorizeResponse response;

            try{
                response = Retry.retries(3,
                        Duration.ofMillis(150), ()->

                            paymentProvider.authorize(
                                    new PaymentProviderAuthorizeCmd(
                                            request.order_id(),
                                            request.customer_id(),
                                            request.amount_cents(),
                                            request.currency(),
                                            idempotencyKey,
                                            Map.of("payment", "stripe-pay")
                                    )
                            )

                );


            } catch (Exception e) {
                handleAuthorizationFailure(transactionTemplate, payment !=null ? payment.getId() : null, idempotencyCommand, e);
                throw e;
            }
            return transactionTemplate.execute(status -> {

                com.org.persistence.entities.Payment p  = paymentRepository.findByIdForUpdate(payment != null ? payment.getId() : null)
                        .orElseThrow(()-> new IllegalStateException(""));

                p.markAuthorized(
                        response.providerTxnId(),
                        response.createdAt() != null? response.createdAt() : Instant.now(),
                        response.expiresAt() != null? response.expiresAt() :
                                Instant.now().plus(7, ChronoUnit.DAYS)

                );

                AuthorizeResponse authorizeResponse = new AuthorizeResponse(
                        p.getAuthorizationReference(),
                        "AUTHORIZED",
                        p.getId().toString()

                );

                idempotencyService.complete(idempotencyCommand, authorizeResponse);

                return authorizeResponse;
            });
}

    private void handleAuthorizationFailure(TransactionTemplate transactionTemplate, UUID uuid, IdempotencyCommand idempotencyCommand, Exception e) {
        transactionTemplate.executeWithoutResult(status -> {
            com.org.persistence.entities.Payment payment = paymentRepository.findByIdForUpdate(uuid)
                    .orElseThrow(() -> new IllegalStateException("Payment with id " + uuid + " not found"));

            Payment paymentMap = paymentMapper.toPayment(payment);

            paymentMap.markFailed("AUTHORIZED_FAILURE", e.getMessage());

            paymentRepository.save(payment);

            AuthorizeResponse failedResponse =
                    new AuthorizeResponse(
                            payment.getId().toString(),
                            "FAILED",
                            null
                    );

            idempotencyService.fail(idempotencyCommand, failedResponse);
        });
    }
    }