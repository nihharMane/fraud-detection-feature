package com.Validation.payments.fraud.rules;


import com.Validation.payments.Entity.UserTransactionStatsEntity;
import com.Validation.payments.fraud.FraudRuleResult;
import com.Validation.payments.pojo.PaymentRequest;
import com.Validation.payments.service.TestDataBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FraudRulesTest {

    // ---------- AmountDeviationRule ----------

    @Test
    void amountDeviation_skipsWhenNoHistory() {
        AmountDeviationRule rule = new AmountDeviationRule();
        PaymentRequest request = TestDataBuilder.buildRequest(); // amount = 100

        FraudRuleResult result = rule.evaluate(request, null, 0);

        assertFalse(result.isTriggered());
    }

    @Test
    void amountDeviation_triggersOnLargeSpike() {
        AmountDeviationRule rule = new AmountDeviationRule();
        PaymentRequest request = TestDataBuilder.buildRequest();
        request.getPayment().setAmount(10_000); // way above the user's usual spend

        UserTransactionStatsEntity stats = new UserTransactionStatsEntity();
        stats.setEndUserID("user123");
        stats.setTransactionCount(10);
        stats.setAvgAmount(150);
        stats.setStdDevAmount(20);

        FraudRuleResult result = rule.evaluate(request, stats, 1);

        assertTrue(result.isTriggered());
        assertEquals("AMOUNT_DEVIATION", result.getRuleName());
        assertTrue(result.getPoints() > 0);
    }

    @Test
    void amountDeviation_doesNotTriggerWithinNormalRange() {
        AmountDeviationRule rule = new AmountDeviationRule();
        PaymentRequest request = TestDataBuilder.buildRequest();
        request.getPayment().setAmount(160);

        UserTransactionStatsEntity stats = new UserTransactionStatsEntity();
        stats.setTransactionCount(10);
        stats.setAvgAmount(150);
        stats.setStdDevAmount(20);

        FraudRuleResult result = rule.evaluate(request, stats, 1);

        assertFalse(result.isTriggered());
    }

    // ---------- VelocityRule ----------

    @Test
    void velocity_triggersWhenOverLimit() {
        VelocityRule rule = new VelocityRule();
        ReflectionTestUtils.setField(rule, "maxTxnPerHour", 5);

        PaymentRequest request = TestDataBuilder.buildRequest();

        FraudRuleResult result = rule.evaluate(request, null, 8);

        assertTrue(result.isTriggered());
        assertEquals("VELOCITY_BREACH", result.getRuleName());
    }

    @Test
    void velocity_doesNotTriggerUnderLimit() {
        VelocityRule rule = new VelocityRule();
        ReflectionTestUtils.setField(rule, "maxTxnPerHour", 5);

        PaymentRequest request = TestDataBuilder.buildRequest();

        FraudRuleResult result = rule.evaluate(request, null, 3);

        assertFalse(result.isTriggered());
    }

    // ---------- MerchantMismatchRule ----------

    @Test
    void merchantMismatch_skipsForNewUser() {
        MerchantMismatchRule rule = new MerchantMismatchRule();
        PaymentRequest request = TestDataBuilder.buildRequest(); // paymentMethod = APM

        FraudRuleResult result = rule.evaluate(request, null, 0);

        assertFalse(result.isTriggered());
    }

    @Test
    void merchantMismatch_triggersOnUnseenPaymentMethod() {
        MerchantMismatchRule rule = new MerchantMismatchRule();
        PaymentRequest request = TestDataBuilder.buildRequest();
        request.getPayment().setPaymentMethod("UPI");

        UserTransactionStatsEntity stats = new UserTransactionStatsEntity();
        stats.setKnownMerchantCategories("CARD,APM");
        stats.setLastTransactionAt(LocalDateTime.now());

        FraudRuleResult result = rule.evaluate(request, stats, 1);

        assertTrue(result.isTriggered());
        assertEquals("MERCHANT_MISMATCH", result.getRuleName());
    }

    @Test
    void merchantMismatch_doesNotTriggerForKnownMethod() {
        MerchantMismatchRule rule = new MerchantMismatchRule();
        PaymentRequest request = TestDataBuilder.buildRequest();
        request.getPayment().setPaymentMethod("APM");

        UserTransactionStatsEntity stats = new UserTransactionStatsEntity();
        stats.setKnownMerchantCategories("CARD,APM");

        FraudRuleResult result = rule.evaluate(request, stats, 1);

        assertFalse(result.isTriggered());
    }
}
