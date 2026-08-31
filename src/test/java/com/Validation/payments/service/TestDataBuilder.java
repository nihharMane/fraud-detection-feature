package com.Validation.payments.service;


import com.Validation.payments.pojo.LineItem;
import com.Validation.payments.pojo.Payment;
import com.Validation.payments.pojo.PaymentRequest;
import com.Validation.payments.pojo.User;

import java.util.Arrays;

public class TestDataBuilder {

    public static PaymentRequest buildRequest() {

        // -------- USER --------
        User user = new User();
        user.setEndUserID("user123");
        user.setFirstname("john");
        user.setLastname("Doe");
        user.setEmail("john.doe@example.com");
        user.setMobilePhone("+1234567890");

        // -------- LINE ITEMS --------
        LineItem item1 = new LineItem();
        item1.setCurrency("EUR");
        item1.setProductName("Phone");
        item1.setUnitAmount(200);
        item1.setQuantity(1);

        LineItem item2 = new LineItem();
        item2.setCurrency("EUR");
        item2.setProductName("Headphones");
        item2.setUnitAmount(500);
        item2.setQuantity(2);

        // -------- PAYMENT --------
        Payment payment = new Payment();
        payment.setCurrency("USD");
        payment.setAmount(100);
        payment.setBrandName("MyShop");
        payment.setLocale("en-US");
        payment.setCountry("US");

        payment.setMerchantTxnRef("TXN1234560017");
        payment.setPaymentMethod("APM");
        payment.setProvider("STRIPE");
        payment.setPaymentType("SALE");

        payment.setSuccessUrl("https://example.com/success");
        payment.setCancelUrl("https://example.com/cancel");

        payment.setLineItems(Arrays.asList(item1, item2));

        // -------- REQUEST --------
        PaymentRequest request = new PaymentRequest();
        request.setUser(user);
        request.setPayment(payment);

        return request;
    }
}