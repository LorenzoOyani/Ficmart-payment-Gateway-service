package com.org.gateway.controller;

import com.org.application.dto.AuthorizeResponse;
import com.org.application.usecase.GatewayAuthorizationService;
import com.org.domain.dto.AuthorizeRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/paymentAuthorization/v1")
public class PaymentController {

    public final GatewayAuthorizationService  authorizationService;


    @PostMapping("/authorize-payment")
    public ResponseEntity<?> authorizePayment(@RequestHeader("idempotency-key") String idempotencyKey, @Valid @RequestBody AuthorizeRequest authorizeRequest){

        AuthorizeResponse response = authorizationService.authorizePayment(authorizeRequest , idempotencyKey);
        return ResponseEntity.ok(response);

    }
}
