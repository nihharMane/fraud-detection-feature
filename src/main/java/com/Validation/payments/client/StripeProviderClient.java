package com.Validation.payments.client;

import com.Validation.payments.client.dto.StripeCreatePaymentRequest;
import com.Validation.payments.client.dto.StripePaymentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class StripeProviderClient {

    private final RestClient restClient;

    public StripeProviderClient(
            RestClient.Builder restClientBuilder,
            @Value("${stripe.provider.url}") String stripeProviderUrl) {

        this.restClient = restClientBuilder
                .baseUrl(stripeProviderUrl)
                .build();
    }

    public StripePaymentResponse createPayment(
            StripeCreatePaymentRequest request) {

        return restClient.post()
                .uri("/v1/payments")
                .body(request)
                .retrieve()
                .body(StripePaymentResponse.class);
    }
}