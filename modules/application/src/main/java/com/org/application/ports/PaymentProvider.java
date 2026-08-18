package com.org.application.ports;


import com.org.application.dto.CancelCommand;
import com.org.application.dto.CancelRequest;
import com.org.application.dto.CancelResponse;
import com.org.application.dto.providerDTO.*;
import com.org.domain.dto.PaymentAuthorizeResponse;
import com.org.domain.dto.VoidResult;

public interface PaymentProvider {

    PaymentAuthorizeResponse authorize(PaymentProviderAuthorizeCmd paymentAuthorizeCmd);

    ProviderCaptureResult capture(ProviderCaptureCommand cmd);

    VoidResult voidPayment(ProviderVoidCommand cmd);

    ProviderRefundResult refund(ProviderRefundCommand cmd);

    CancelResponse cancel(CancelCommand command);

}

