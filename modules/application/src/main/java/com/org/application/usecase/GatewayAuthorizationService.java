package com.org.application.usecase;

import com.org.application.dto.*;
import com.org.application.dto.providerDTO.PaymentProviderAuthorizeCmd;
import com.org.application.dto.providerDTO.ProviderRefundCommand;
import com.org.application.dto.providerDTO.ProviderVoidCommand;
import com.org.application.mapper.PaymentMapper;
import com.org.application.ports.PaymentProvider;
import com.org.application.util.Hashing;
import com.org.application.util.Retry;
import com.org.domain.dto.*;
import com.org.domain.exception.IdempotencyIdentityConflictException;
import com.org.domain.model.IdempotencyState;
import com.org.domain.model.Payment;
import com.org.infrastructure.service.IdempotencyService;
import com.org.persistence.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class GatewayAuthorizationService {

    private static final String AUTHORIZE_OPERATION = "PAYMENT_AUTHORIZE";
    private static final String REFUND_OPERATION = "PAYMENT_REFUND";
    private static final String CANCEL_OPERATION = "PAYMENT_CANCEL";

    @Qualifier("stripePaymentProvider")
    private final PaymentProvider paymentProvider;

    private final TransactionTemplate transactionTemplate;
    private final IdempotencyService idempotencyService;
    private final PaymentMapper paymentMapper;
    private final PaymentRepository paymentRepository;


    @SuppressWarnings("Unchecked")
    public AuthorizeResponse authorizePayment(
            AuthorizeRequest request,
            String idempotencyKey
    ) throws IdempotencyIdentityConflictException {

        final String merchantId = "x-merchantId";

        IdempotencyCommand idempotencyCommand = buildIdempotencyCommand(
                merchantId,
                AUTHORIZE_OPERATION,
                idempotencyKey,
                request.order_id() + "|" +
                        request.customer_id() + "|" +
                        request.amount_cents() + "|" +
                        request.currency()
        );

        IdempotencyState idempotencyState = idempotencyService.begin(idempotencyCommand);

        if (idempotencyState.isReplay()) {
            return (AuthorizeResponse) idempotencyState.getPaymentResponse();
        }

        if (idempotencyState.isInProgress()) {
            return new AuthorizeResponse(
                    null,
                    "IN_PROGRESS",
                    "Authorization request with this idempotency key is already in progress"
            );
        }

        com.org.persistence.entities.Payment payment = transactionTemplate.execute(status -> {
            Payment pendingPayment = Payment.newPending(
                    request.customer_id(),
                    request.order_id(),
                    merchantId,
                    request.amount_cents(),
                    request.currency()
            );

            return paymentRepository.save(paymentMapper.toPaymentEntity(pendingPayment));
        });

        PaymentAuthorizeResponse authorizeResponse = null;
        try {
           authorizeResponse =    Retry.retries(
                    3,
                    Duration.ofMillis(150),
                    () -> paymentProvider.authorize(
                            new PaymentProviderAuthorizeCmd(
                                    request.order_id(),
                                    request.customer_id(),
                                    request.amount_cents(),
                                    request.currency(),
                                    idempotencyKey,
                                    Map.of("payment", "stripe-pay"),
                                    Instant.now()
                            )
                    )
            );
        } catch (Exception ex) {
            if (payment != null){
                handleAuthorizationFailure(payment.getId(), idempotencyCommand, ex);
                throw ex;
            }

        }

        PaymentAuthorizeResponse finalAuthorizeResponse = authorizeResponse;
        return transactionTemplate.execute(status -> {
            com.org.persistence.entities.Payment lockedPayment =
                    paymentRepository.findByIdForUpdate(payment.getId())
                            .orElseThrow(() -> new IllegalStateException(
                                    "Payment with id " + payment.getId() + " not found"
                            ));

            lockedPayment.markAuthorized(
                    finalAuthorizeResponse.authorizationReference(),
                    finalAuthorizeResponse.providerTxnId(),
                    finalAuthorizeResponse.createdAt(),
                    finalAuthorizeResponse.expiresAt()

            );

            AuthorizeResponse response = new AuthorizeResponse(
                    lockedPayment.getAuthorizationReference(),
                    "AUTHORIZED",
                    lockedPayment.getId().toString()
            );

            idempotencyService.complete(idempotencyCommand, response);

            return response;
        });
    }

    public RefundResponse refundPayment(
            RefundRequest request,
            String merchantId,
            String idempotencyKey
    ) throws IdempotencyIdentityConflictException {

        IdempotencyCommand idempotencyCommand = buildIdempotencyCommand(
                merchantId,
                REFUND_OPERATION,
                idempotencyKey,
                request.transactionId() + "|" + request.amount()
        );

        IdempotencyState idempotencyState = idempotencyService.begin(idempotencyCommand);

        if (idempotencyState.isReplay()) {
            return (RefundResponse) idempotencyState.getPaymentResponse();
        }

        if (idempotencyState.isInProgress()) {
            return new RefundResponse(
                    request.transactionId(),
                    null,
                    "IN_PROGRESS",
                    "Refund request with this idempotency key is already in progress"
            );
        }

        UUID paymentId = UUID.fromString(request.transactionId());

        String providerAuthorizationId = transactionTemplate.execute(status ->
                paymentRepository.findByIdForUpdate(paymentId)
                        .orElseThrow(() -> new IllegalStateException(
                                "Payment with id " + paymentId + " not found"
                        ))
                        .getProviderAuthorizationId()
        );

        try {
            Retry.retries(
                    3,
                    Duration.ofMillis(150),
                    () -> paymentProvider.refund(
                            new ProviderRefundCommand(
                                    providerAuthorizationId,
                                    request.orderId(),
                                    request.customerId(),
                                    request.amount(),
                                    request.currency(),
                                    idempotencyKey
                            )
                    )
            );
        } catch (Exception ex) {
            RefundResponse refundResponse = new RefundResponse(
                    request.transactionId(),
                    null,
                    "FAILED",
                    ex.getMessage()
            );

            idempotencyService.fail(idempotencyCommand, refundResponse);
            throw ex;
        }

        return transactionTemplate.execute(status -> {
            com.org.persistence.entities.Payment payment =
                    paymentRepository.findByIdForUpdate(paymentId)
                            .orElseThrow(() -> new IllegalStateException(
                                    "Payment with id " + paymentId + " not found"
                            ));

            payment.markRefunded(paymentId);

            RefundResponse response = new RefundResponse(
                    payment.getProviderTransactionId(),
                    payment.getMerchantId(),
                    "REFUNDED",
                    null
            );

            idempotencyService.complete(idempotencyCommand, response);

            return response;
        });
    }

    public CancelResponse cancelPayment(
            CancelRequest request,
            String merchantId,
            String idempotencyKey
    ) throws IdempotencyIdentityConflictException {

        IdempotencyCommand idempotencyCommand = buildIdempotencyCommand(
                merchantId,
                CANCEL_OPERATION,
                idempotencyKey,
                request.paymentId() + "|" + request.reason()
        );

        IdempotencyState idempotencyState = idempotencyService.begin(idempotencyCommand);

        if (idempotencyState.isReplay()) {
            return (CancelResponse) idempotencyState.getPaymentResponse();
        }

        if (idempotencyState.isInProgress()) {
            return new CancelResponse(
                    request.paymentId(),
                    "IN_PROGRESS",
                    "Cancel request with this idempotency key is already in progress"
            );
        }

        UUID paymentId = UUID.fromString(request.paymentId());

        String providerAuthorizationId = transactionTemplate.execute(status ->
                paymentRepository.findByIdForUpdate(paymentId)
                        .orElseThrow(() -> new IllegalStateException(
                                "Payment with id " + paymentId + " not found"
                        ))
                        .getProviderAuthorizationId()
        );

        try {

           Retry.retries(
                    3,
                    Duration.ofMillis(150),
                    () -> paymentProvider.voidPayment(
                            new ProviderVoidCommand(
                                    providerAuthorizationId,
                                    request.orderId(),
                                    request.customerId(),
                                    idempotencyKey
                            )
                    )
            );
        } catch (Exception ex) {
            CancelResponse failedResponse = new CancelResponse(
                    request.paymentId(),
                    "FAILED",
                    ex.getMessage()
            );

            idempotencyService.fail(idempotencyCommand, failedResponse);
            throw ex;
        }

        return transactionTemplate.execute(status -> {
            com.org.persistence.entities.Payment payment =
                    paymentRepository.findByIdForUpdate(paymentId)
                            .orElseThrow(() -> new IllegalStateException(
                                    "Payment with id " + paymentId + " not found"
                            ));

            payment.markVoided();

            CancelResponse response = new CancelResponse(
                    payment.getProviderTransactionId(),
                    "cancelled",
                    "Aborted transaction"
            );

            idempotencyService.complete(idempotencyCommand, response);

            return response;
        });
    }

    private IdempotencyCommand buildIdempotencyCommand(
            String merchantId,
            String operation,
            String idempotencyKey,
            String identity
    ) {
        String requestHash = Hashing.sha(operation + "|" + identity + "|" + idempotencyKey);

        return IdempotencyCommand.of(
                merchantId,
                requestHash,
                operation,
                Instant.now(),
                idempotencyKey
        );
    }

    private void handleAuthorizationFailure(
            UUID paymentId,
            IdempotencyCommand idempotencyCommand,
            Exception ex
    ) {
        transactionTemplate.executeWithoutResult(status -> {
            com.org.persistence.entities.Payment payment =
                    paymentRepository.findByIdForUpdate(paymentId)
                            .orElseThrow(() -> new IllegalStateException(
                                    "Payment with id " + paymentId + " not found"
                            ));

            payment.markFailed();

            AuthorizeResponse failedResponse = new AuthorizeResponse(
                    payment.getId().toString(),
                    "FAILED",
                    ex.getMessage()
            );

            idempotencyService.fail(idempotencyCommand, failedResponse);
        });
    }
}