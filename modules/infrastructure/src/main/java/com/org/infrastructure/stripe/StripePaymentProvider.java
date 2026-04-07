package com.org.infrastructure.stripe;

import com.org.application.dto.*;
import com.org.application.ports.PaymentProvider;
import com.org.domain.dto.PaymentAuthorizeResponse;

public class StripePaymentProvider implements PaymentProvider {

    @Override
    public PaymentAuthorizeResponse authorize(PaymentProviderAuthorizeCmd paymentAuthorizeCmd) {
        return null;
    }

    @Override
    public ProviderCaptureResult capture(ProviderCaptureCommand cmd) {
        return null;
    }

    @Override
    public ProviderVoidResult voidAuth(ProviderVoidCommand cmd) {
        return null;
    }

    @Override
    public ProviderRefundResult refund(ProviderRefundCommand cmd) {
        return null;
    }
}
