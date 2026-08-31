package com.Validation.payments.fraud.rules;

import com.Validation.payments.Entity.UserTransactionStatsEntity;
import com.Validation.payments.fraud.FraudRule;
import com.Validation.payments.fraud.FraudRuleResult;
import com.Validation.payments.pojo.PaymentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AmountDeviationRule implements FraudRule {

    private static final double Z_SCORE_THRESHOLD = 3.0;
    private static final int MAX_POINTS = 40;

    @Override
    public FraudRuleResult evaluate(PaymentRequest paymentRequest, UserTransactionStatsEntity stats, long txnCountLastHour) {

        long amount = paymentRequest.getPayment().getAmount();

        // Not enough history yet - skip this rule rather than penalize a new user
        if (stats == null || stats.getTransactionCount() < 3 || stats.getStdDevAmount() == 0) {
            log.debug("Insufficient history for amount deviation check, endUserID: {}",
                    paymentRequest.getUser().getEndUserID());
            return FraudRuleResult.notTriggered("AMOUNT_DEVIATION");
        }

        double zScore = (amount - stats.getAvgAmount()) / stats.getStdDevAmount();

        if (zScore <= Z_SCORE_THRESHOLD) {
            return FraudRuleResult.notTriggered("AMOUNT_DEVIATION");
        }

        int points = (int) Math.min(MAX_POINTS, Math.round(zScore * 8));
        String details = String.format(
                "Amount %d is %.1fx std-dev above user's average of %.2f",
                amount, zScore, stats.getAvgAmount());

        log.info("AMOUNT_DEVIATION triggered for endUserID: {} | {}",
                paymentRequest.getUser().getEndUserID(), details);

        return new FraudRuleResult("AMOUNT_DEVIATION", true, points, details);
    }
}
