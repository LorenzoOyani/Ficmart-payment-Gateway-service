package com.org.gateway.controller;

import com.org.domain.dto.AuthorizeRequest;
import com.org.domain.dto.PaymentResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/paymentAuthorization/v1")
public class PaymentController {



    @PostMapping("/authorize-payment")
    public ResponseEntity<?> authorizePayment(@RequestHeader("idempotency-key") String idempotencyKey, @Valid @RequestBody AuthorizeRequest authorizeRequest){

        
        PaymentResponse paymentResponse = new PaymentResponse();
        return ResponseEntity.ok(paymentResponse);

    }
}
