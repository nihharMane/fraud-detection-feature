package com.Validation.payments.fraud;

import com.Validation.payments.Entity.UserTransactionStatsEntity;
import com.Validation.payments.pojo.PaymentRequest;
import com.Validation.payments.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VelocityFraudRule implements FraudRule {

    private final RedisService redisService;

    private static final long WINDOW_SECONDS = 60;
    private static final long TRANSACTION_LIMIT = 5;
    private static final int RISK_POINTS = 30;

    @Override
    public FraudRuleResult evaluate(
            PaymentRequest paymentRequest,
            UserTransactionStatsEntity stats,
            long txnCountLastHour) {

        String endUserID = paymentRequest.getUser().getEndUserID();
        String merchantTxnRef = paymentRequest.getPayment().getMerchantTxnRef();

        String redisKey = "fraud:velocity:" + endUserID;

        long transactionCount =
                redisService.addTransactionAndCount(
                        redisKey,
                        merchantTxnRef
                );

        if (transactionCount > TRANSACTION_LIMIT) {

            return new FraudRuleResult(
                    "TRANSACTION_VELOCITY",
                    true,
                    RISK_POINTS,
                    transactionCount +
                            " transactions detected for user " +
                            endUserID +
                            " within " +
                            WINDOW_SECONDS +
                            " seconds"
            );
        }

        return FraudRuleResult.notTriggered("TRANSACTION_VELOCITY");
    }
}