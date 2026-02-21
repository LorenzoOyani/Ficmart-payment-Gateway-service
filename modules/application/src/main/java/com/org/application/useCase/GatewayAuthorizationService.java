package com.org.application.useCase;

import com.org.application.mapper.PaymentMapper;
import com.org.application.ports.PaymentProvider;
import com.org.application.ports.PaymentProviderAuthorizeCmd;
import com.org.application.ports.ProviderAuthorizeResponse;
import com.org.application.util.Hashing;
import com.org.domain.dto.*;
import com.org.domain.dto.IdempotencyService;
//import com.org.infrastructure.util.Retry;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.org.application.util.Retry;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class GatewayAuthorizationService {


    private final IdempotencyService idempotencyService;

    private final PaymentProvider paymentProvider;


    @Transactional
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
                request.order_id() + "|" + request.customer_id() + "|" + request.amount_cents() + "|USD" + "|" + endpoint);

        IdempotencyCommand idempotencyCommand =
                IdempotencyCommand.of(
                        "PAYMENT_AUTHORIZE",
                        requestHash,
                        authorizeCommand.idempotencyKey().toString(),
                        Instant.now()
                );

        return idempotencyService.execute(
                idempotencyCommand,
                () -> {
                    ProviderAuthorizeResponse providerRes = Retry.retries(
                            3,
                            Duration.ofMillis(150),
                            () -> paymentProvider.authorize(
                                    new PaymentProviderAuthorizeCmd(
                                            authorizeCommand.orderId(),
                                            authorizeCommand.customerId(),
                                            authorizeCommand.amount(),
                                            "USD",
                                            new IdempotencyKey(authorizeCommand.idempotencyKey().toString()),
                                            Map.of("payment", "ficMart-pay!")
                                    )
                            )
                    );

                    return new AuthorizeResponse(
                            providerRes.status(),
                            providerRes.providerTxnId(),
                            providerRes.providerTxnId()
                    );
                },
                AuthorizeResponse.class
        );
    }
}