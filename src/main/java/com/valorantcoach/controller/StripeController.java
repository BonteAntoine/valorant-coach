package com.valorantcoach.controller;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.valorantcoach.model.User;
import com.valorantcoach.repository.UserRepository;
import com.valorantcoach.security.JwtUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/stripe")
@CrossOrigin(origins = "*")
public class StripeController {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.price.id}")
    private String stripePriceId;

    @Value("${stripe.webhook.secret:}")
    private String webhookSecret;

    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public StripeController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    // Crée une session de paiement Stripe Checkout
    @PostMapping("/checkout")
    public ResponseEntity<?> createCheckout(
            @RequestHeader("Authorization") String authHeader) throws Exception {

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setCustomerEmail(email)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setPrice(stripePriceId)
                        .setQuantity(1L)
                        .build())
                .setSuccessUrl(appUrl + "/?subscribed=true")
                .setCancelUrl(appUrl + "/?canceled=true")
                .putMetadata("email", email)
                .build();

        Session session = Session.create(params);
        return ResponseEntity.ok(Map.of("url", session.getUrl()));
    }

    // Webhook Stripe — appelé automatiquement après un paiement
    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {

        if (webhookSecret.isEmpty()) {
            // Mode dev sans webhook secret — on accepte direct
            return ResponseEntity.ok("ok");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().body("Signature invalide");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer()
                    .getObject().orElse(null);
            if (session != null) {
                String email = session.getMetadata().get("email");
                Optional<User> userOpt = userRepository.findByEmail(email);
                userOpt.ifPresent(user -> {
                    user.setSubscribed(true);
                    user.setStripeCustomerId(session.getCustomer());
                    userRepository.save(user);
                });
            }
        }

        if ("customer.subscription.deleted".equals(event.getType())) {
            // Abonnement annulé — on retire l'accès
            String customerId = event.getData().getObject().toString();
            userRepository.findAll().stream()
                    .filter(u -> customerId.equals(u.getStripeCustomerId()))
                    .findFirst()
                    .ifPresent(u -> {
                        u.setSubscribed(false);
                        userRepository.save(u);
                    });
        }

        return ResponseEntity.ok("ok");
    }
}
