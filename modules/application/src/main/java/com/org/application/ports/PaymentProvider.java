package com.org.application.ports;


import com.org.application.dto.*;

public interface PaymentProvider {

    AuthorizeResponse authorize(PaymentProviderAuthorizeCmd paymentAuthorizeCmd);

    ProviderCaptureResult capture(ProviderCaptureCommand cmd);

    ProviderVoidResult voidAuth(ProviderVoidCommand cmd);

    ProviderRefundResult refund(ProviderRefundCommand cmd);

}

