package com.org.infrastructure.configuration;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Getter
@Configuration
@EnableConfigurationProperties(StripeProperties.class)
public class StripeConfig {

    private final StripeProperties properties;

    @Autowired
    public StripeConfig(StripeProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init(){
        Stripe.apiKey = properties.secretKey();
    }
}
