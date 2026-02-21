package com.org.application.ports;



public interface PaymentProvider {

    ProviderAuthorizeResponse authorize(PaymentProviderAuthorizeCmd paymentAuthorizeCmd);

    ProviderCaptureResult capture(ProviderCaptureCommand cmd);

    ProviderVoidResult voidAuth(ProviderVoidCommand cmd);

    ProviderRefundResult refund(ProviderRefundCommand cmd);

}

