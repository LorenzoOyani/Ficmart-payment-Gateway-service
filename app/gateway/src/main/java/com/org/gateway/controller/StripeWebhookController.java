package com.org.gateway.controller;

import com.org.infrastructure.service.StripeWebhookCreationService;
import com.org.persistence.repository.StripeWebhookRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/V1/webhooks/stripe")
public class StripeWebhookController {

    private final StripeWebhookRepository webhookRepository;
    private final StripeWebhookCreationService stripeWebhookCreationService;

    @Value("${app.stripe.secret-key}")
    private String webhookSecret;

    @PostMapping
    public ResponseEntity<String> receiveStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String stripeSignature
    ) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, stripeSignature, webhookSecret);
        } catch (SignatureVerificationException ex) {
            return ResponseEntity.badRequest().body("Invalid Stripe signature");
        }

        if (webhookRepository.existsByStripeEventId(event.getId())) {
            return ResponseEntity.ok("Duplicate event ignored");
        }


        stripeWebhookCreationService.ingest(event, payload);

        return ResponseEntity.ok("Webhook received");
    }
}




