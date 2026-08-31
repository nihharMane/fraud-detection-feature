package com.Validation.payments.fraud.rules;

import com.Validation.payments.Entity.UserTransactionStatsEntity;
import com.Validation.payments.fraud.FraudRule;
import com.Validation.payments.fraud.FraudRuleResult;
import com.Validation.payments.pojo.PaymentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Flags a payment method the user has never used before.
 * NOTE: uses paymentMethod (CARD/UPI/APM) as a proxy "category" for now -
 * swap in a real merchant-category field once that's captured upstream.
 */
@Slf4j
@Component
public class MerchantMismatchRule implements FraudRule {

    private static final int POINTS = 20;

    @Override
    public FraudRuleResult evaluate(PaymentRequest paymentRequest, UserTransactionStatsEntity stats, long txnCountLastHour) {

        String currentMethod = paymentRequest.getPayment().getPaymentMethod();

        // New user or no history recorded yet - nothing to compare against
        if (stats == null || stats.getKnownMerchantCategories() == null
                || stats.getKnownMerchantCategories().isBlank()) {
            return FraudRuleResult.notTriggered("MERCHANT_MISMATCH");
        }

        Set<String> knownMethods = Arrays.stream(stats.getKnownMerchantCategories().split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        if (knownMethods.contains(currentMethod)) {
            return FraudRuleResult.notTriggered("MERCHANT_MISMATCH");
        }

        String details = String.format(
                "Payment method %s has never been used before by this user (known: %s)",
                currentMethod, knownMethods);

        log.info("MERCHANT_MISMATCH triggered for endUserID: {} | {}",
                paymentRequest.getUser().getEndUserID(), details);

        return new FraudRuleResult("MERCHANT_MISMATCH", true, POINTS, details);
    }
}
