package com.Validation.payments.fraud.rules;

import com.Validation.payments.Entity.UserTransactionStatsEntity;
import com.Validation.payments.fraud.FraudRule;
import com.Validation.payments.fraud.FraudRuleResult;
import com.Validation.payments.pojo.PaymentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class VelocityRule implements FraudRule {

    @Value("${fraud.velocity.max-txn-per-hour:5}")
    private int maxTxnPerHour;

    private static final int MAX_POINTS = 35;

    @Override
    public FraudRuleResult evaluate(PaymentRequest paymentRequest, UserTransactionStatsEntity stats, long txnCountLastHour) {

        if (txnCountLastHour <= maxTxnPerHour) {
            return FraudRuleResult.notTriggered("VELOCITY_BREACH");
        }

        int overBy = (int) (txnCountLastHour - maxTxnPerHour);
        int points = Math.min(MAX_POINTS, 15 + overBy * 5);

        String details = String.format(
                "%d transactions in the last hour, exceeding the limit of %d",
                txnCountLastHour, maxTxnPerHour);

        log.info("VELOCITY_BREACH triggered for endUserID: {} | {}",
                paymentRequest.getUser().getEndUserID(), details);

        return new FraudRuleResult("VELOCITY_BREACH", true, points, details);
    }
}
