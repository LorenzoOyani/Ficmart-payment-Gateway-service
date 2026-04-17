package com.org.application.ports;


import com.org.application.dto.*;
import com.org.domain.dto.PaymentAuthorizeResponse;
import com.org.domain.dto.VoidResult;

public interface PaymentProvider {

    PaymentAuthorizeResponse authorize(PaymentProviderAuthorizeCmd paymentAuthorizeCmd);

    ProviderCaptureResult capture(ProviderCaptureCommand cmd);

    VoidResult voidAuth(ProviderVoidCommand cmd);

    ProviderRefundResult refund(ProviderRefundCommand cmd);

}

