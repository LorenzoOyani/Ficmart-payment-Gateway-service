package com.org.infrastructure.stripe;

import com.org.application.dto.CancelCommand;
import com.org.application.dto.CancelResponse;
import com.org.application.dto.providerDTO.*;
import com.org.application.ports.PaymentProvider;
import com.org.domain.dto.PaymentAuthorizeResponse;
import com.org.domain.dto.VoidResult;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.StripeClient;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class StripePaymentProvider implements PaymentProvider {

    private final Logger log  = LoggerFactory.getLogger(StripePaymentProvider.class);

    private final StripeClient stripeClient;

    @Autowired
    public StripePaymentProvider(StripeClient stripeClient) {
        this.stripeClient = stripeClient;
    }

    @Override
    public PaymentAuthorizeResponse authorize(PaymentProviderAuthorizeCmd cmd) {

        try{

            Map<String,  String> params = new HashMap<>(cmd.metadata());
            params.put("orderId", cmd.orderId());
            params.put("customerId", cmd.customerId());

            PaymentIntentCreateParams intent = PaymentIntentCreateParams.builder()
                    .setAmount(cmd.amount_cent())
                    .setCurrency(cmd.currency())
                    .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.MANUAL)
                    .putAllMetadata(params)
                    .build();

            RequestOptions requestOptions = RequestOptions.builder()
                    .setIdempotencyKey("authorize" +cmd.idempotencyKey())
                    .build();

            PaymentIntent pi = stripeClient.v1().paymentIntents().create(intent, requestOptions);

            Instant now = Instant.ofEpochSecond(pi.getCreated());

            return new PaymentAuthorizeResponse(
                    mapStatusToStripe(pi.getStatus()),
                    pi.getId(),
                    pi.getId(),
                    now,
                    now.plusSeconds(7 * 24 * 60 * 60)

            );
        }catch(StripeException e){
            throw new IllegalStateException(e.getMessage());
        }
    }

    private String mapStatusToStripe(String status) {
        return switch (status) {
            case "requires_capture", "processing", "succeeded" -> "AUTHORIZED";
            case "requires_payment_method", "canceled" -> "FAILED";
            default -> "PROCESSING";
        };
    }

    @Override
    public ProviderCaptureResult capture(ProviderCaptureCommand cmd) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(cmd.authorizationReference());

            RequestOptions requestOptions = RequestOptions.builder()
                    .setIdempotencyKey(cmd.idempotencyKey())
                    .build();
            PaymentIntent captured = intent.capture(requestOptions);

            return new ProviderCaptureResult(
                    captured.getStatus(),
                    captured.getId(),
                    captured.getId(),
                    Instant.ofEpochSecond(captured.getCreated())
            );
        } catch (StripeException ex) {
            throw new IllegalStateException("Stripe capture failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public VoidResult voidPayment(ProviderVoidCommand cmd) {
        try{
            PaymentIntent pi = stripeClient.v1().paymentIntents().cancel(
                    cmd.providerAuthId()
            );
            return new VoidResult(pi.getId(), pi.getStatus());

        } catch (StripeException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public ProviderRefundResult refund(ProviderRefundCommand cmd) {
        try{
            RefundCreateParams.Builder refBuilder = RefundCreateParams.builder()
                    .setPaymentIntent(cmd.providerAuthId());

            if (cmd.amountCents() != null) {
                refBuilder.setAmount(cmd.amountCents());

            }
            Refund refund =  stripeClient.v1().refunds().create(refBuilder.build(),
                    RequestOptions.builder().setIdempotencyKey(cmd.idempotencyKey()).build());

            return new ProviderRefundResult(
                    cmd.providerAuthId(),
                    refund.getStatus(),
                    refund.getId()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);

        }
    }

    @Override
    public CancelResponse cancel(CancelCommand command) {
        try {
            PaymentIntent intent = stripeClient.v1().paymentIntents().cancel(
                    command.paymentId(),
                    RequestOptions.builder()
                            .setIdempotencyKey("cancel:" + command.idempotencyKey())
                            .build()
            );

            return new CancelResponse(
                    intent.getId(),
                    mapPaymentIntentStatus(intent.getStatus()),
                    null
            );
        } catch (StripeException ex) {
            log.error("Stripe cancel failed for paymentIntent={}", command.paymentId(), ex);
            throw StripeGatewayException.from(ex);
        }
    }

    private String mapPaymentIntentStatus(String status) {
        return switch (status) {
            case "requires_capture" -> "AUTHORIZED";
            case "succeeded" -> "CAPTURED";
            case "processing" -> "PROCESSING";
            case "canceled" -> "CANCELLED";
            case "requires_payment_method" -> "FAILED";
            default -> "UNKNOWN";
        };
    }
}
