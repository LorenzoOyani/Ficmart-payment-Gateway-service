package com.org.infrastructure.bankClient;

import com.org.application.dto.CancelCommand;
import com.org.application.dto.CancelResponse;
import com.org.application.dto.providerDTO.*;
import com.org.application.ports.PaymentProvider;
import com.org.domain.dto.PaymentAuthorizeResponse;
import com.org.domain.dto.VoidResult;
import org.springframework.stereotype.Service;

@Service
public class MockBankClient implements PaymentProvider {

    
///  local bank


    @Override
    public PaymentAuthorizeResponse authorize(PaymentProviderAuthorizeCmd paymentAuthorizeCmd) {
        return null;
    }

    @Override
    public ProviderCaptureResult capture(ProviderCaptureCommand cmd) {
        return null;
    }

    @Override
    public VoidResult voidPayment(ProviderVoidCommand cmd) {
        return null;
    }

    @Override
    public ProviderRefundResult refund(ProviderRefundCommand cmd) {
        return null;
    }

    @Override
    public CancelResponse cancel(CancelCommand command) {
        return null;
    }
}
