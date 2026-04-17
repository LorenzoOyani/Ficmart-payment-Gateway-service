package com.org.infrastructure.stripe;

import com.org.application.dto.*;
import com.org.application.ports.PaymentProvider;
import com.org.domain.dto.PaymentAuthorizeResponse;
import com.org.domain.dto.VoidResult;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.StripeClient;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class StripePaymentProvider implements PaymentProvider {


    private final StripeClient stripeClient;

    @Autowired
    public StripePaymentProvider(StripeClient stripeClient) {
        this.stripeClient = stripeClient;
    }

    @Override
    public PaymentAuthorizeResponse authorize(PaymentProviderAuthorizeCmd cmd) {

        try{

            Map<String,  String> params = new HashMap<String, String>(cmd.metadata());
            params.put("orderId", cmd.orderId());
            params.put("customerId", cmd.customerId());

            PaymentIntentCreateParams intent = PaymentIntentCreateParams.builder()
                    .setAmount(cmd.amount_cent())
                    .setCurrency(cmd.currency())
                    .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.MANUAL)
                    .putAllMetadata(params)
                    .build();

            RequestOptions requestOptions = RequestOptions.builder()
                    .setIdempotencyKey(cmd.idempotencyKey())
                    .build();

            PaymentIntent pi = stripeClient.v1().paymentIntents().create(intent, requestOptions);

            Instant now = Instant.ofEpochSecond(cmd.instant().toEpochMilli());

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
    public VoidResult voidAuth(ProviderVoidCommand cmd) {
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
            throw new StripeGatewayException("refund failure");
        }
    }




}
