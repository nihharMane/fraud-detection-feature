package com.Validation.payments.client;

import com.Validation.payments.client.dto.StripeCreatePaymentRequest;
import com.Validation.payments.client.dto.StripeLineItem;
import com.Validation.payments.pojo.LineItem;
import com.Validation.payments.pojo.Payment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StripePaymentMapper {

    public StripeCreatePaymentRequest map(Payment payment) {

        StripeCreatePaymentRequest request =
                new StripeCreatePaymentRequest();

        request.setSuccessUrl(payment.getSuccessUrl());
        request.setCancelUrl(payment.getCancelUrl());

        List<StripeLineItem> stripeLineItems =
                payment.getLineItems()
                        .stream()
                        .map(this::mapLineItem)
                        .toList();

        request.setLineItems(stripeLineItems);

        return request;
    }

    private StripeLineItem mapLineItem(LineItem item) {

        if (item.getUnitAmount() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "Line item amount exceeds Stripe provider limit"
            );
        }

        StripeLineItem stripeItem = new StripeLineItem();

        // Stripe service expects lowercase currency
        stripeItem.setCurrency(item.getCurrency().toLowerCase());

        stripeItem.setProductName(item.getProductName());
        stripeItem.setUnitAmount((int) item.getUnitAmount());
        stripeItem.setQuantity(item.getQuantity());

        return stripeItem;
    }
}