package com.org.infrastructure.service;

import com.org.domain.model.WebhookEventStatus;
import com.org.persistence.entities.Payment;
import com.org.persistence.entities.StripeWebhookEntity;
import com.org.persistence.repository.PaymentRepository;
import com.org.persistence.repository.StripeWebhookRepository;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
    public class StripeWebhookProcessor {

        private final StripeWebhookRepository eventRepository;
        private final PaymentRepository paymentRepository;

        public StripeWebhookProcessor(
                StripeWebhookRepository eventRepository,
                PaymentRepository paymentRepository
        ) {
            this.eventRepository = eventRepository;
            this.paymentRepository = paymentRepository;
        }

        @Transactional
        public void process(UUID dbEventId) {
            StripeWebhookEntity storedEvent = eventRepository.findById(dbEventId)
                    .orElseThrow(() -> new IllegalArgumentException("Webhook event not found"));

            if (storedEvent.getStatus() == WebhookEventStatus.PROCESSED) {
                return;
            }

            storedEvent.setStatus(WebhookEventStatus.PROCESSING);
            eventRepository.save(storedEvent);

            try {
                Event event = Event.GSON.fromJson(storedEvent.getPayload(), Event.class);

                switch (event.getType()) {
                    case "payment_intent.succeeded" -> handlePaymentIntentSucceeded(event);
                    case "payment_intent.payment_failed" -> handlePaymentIntentFailed(event);
                    case "payment_intent.canceled" -> handlePaymentIntentCanceled(event);
                    case "charge.refunded" -> handleChargeRefunded(event);
                    case "refund.updated" -> handleRefundUpdated(event);
                    default -> {
                        // mark processed for ignored event types so they don't keep retrying
                    }
                }

                storedEvent.markProcessed();
                eventRepository.save(storedEvent);

            } catch (Exception ex) {
                storedEvent.markFailed(ex.getMessage());
                eventRepository.save(storedEvent);
                throw ex; // let Kafka retry policy handle it
            }
        }

        private void handlePaymentIntentSucceeded(Event event) {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new IllegalStateException("Missing PaymentIntent"));

            Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntent.getId())
                    .orElseThrow(() -> new IllegalStateException("Payment not found for PI " + paymentIntent.getId()));

            payment.setLatestChargeId(paymentIntent.getLatestCharge());
            payment.markCaptured();
            paymentRepository.save(payment);
        }

        private void handlePaymentIntentFailed(Event event) {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new IllegalStateException("Missing PaymentIntent"));

            Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntent.getId())
                    .orElseThrow(() -> new IllegalStateException("Payment not found for PI " + paymentIntent.getId()));

            payment.markFailed();
            paymentRepository.save(payment);
        }

        private void handlePaymentIntentCanceled(Event event) {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new IllegalStateException("Missing PaymentIntent"));

            Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntent.getId())
                    .orElseThrow(() -> new IllegalStateException("Payment not found for PI " + paymentIntent.getId()));

            payment.markVoided();
            paymentRepository.save(payment);
        }

        private void handleChargeRefunded(Event event) {
            Charge charge = (Charge) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new IllegalStateException("Missing Charge"));

            String paymentIntentId = charge.getPaymentIntent();

            Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new IllegalStateException("Payment not found for PI " + paymentIntentId));

            payment.markRefunded(null);
            paymentRepository.save(payment);
        }

        private void handleRefundUpdated(Event event) {
            Refund refund = (Refund) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new IllegalStateException("Missing Refund"));

            String paymentIntentId = refund.getPaymentIntent();
            Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new IllegalStateException("Payment not found for PI " + paymentIntentId));

            payment.markRefunded(refund.getId());
            paymentRepository.save(payment);
        }
    }

