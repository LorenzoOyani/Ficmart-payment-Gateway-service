package com.org.application.ports;


import com.org.application.dto.*;
import com.org.domain.dto.PaymentAuthorizeResponse;

public interface PaymentProvider {

    PaymentAuthorizeResponse authorize(PaymentProviderAuthorizeCmd paymentAuthorizeCmd);

    ProviderCaptureResult capture(ProviderCaptureCommand cmd);

    ProviderVoidResult voidAuth(ProviderVoidCommand cmd);

    ProviderRefundResult refund(ProviderRefundCommand cmd);

}

