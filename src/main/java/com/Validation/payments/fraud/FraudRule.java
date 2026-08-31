package com.Validation.payments.fraud;

import com.Validation.payments.Entity.UserTransactionStatsEntity;
import com.Validation.payments.pojo.PaymentRequest;

public interface FraudRule {

    /**
     * Evaluates a single fraud signal for the given payment request.
     *
     * @param paymentRequest    the incoming payment request
     * @param stats             the user's historical transaction stats, or null if this is a new user
     * @param txnCountLastHour  number of transactions this user made in the last rolling hour
     */
    FraudRuleResult evaluate(PaymentRequest paymentRequest, UserTransactionStatsEntity stats, long txnCountLastHour);
}
