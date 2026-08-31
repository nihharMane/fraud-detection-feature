package com.Validation.payments.Validator;

import com.Validation.payments.Constants.ErrorCode;
import com.Validation.payments.Entity.RiskScoreEntity;
import com.Validation.payments.Entity.UserTransactionStatsEntity;
import com.Validation.payments.Exception.PaymentValidationException;
import com.Validation.payments.fraud.FraudReasoningService;
import com.Validation.payments.fraud.FraudRule;
import com.Validation.payments.fraud.FraudRuleResult;
import com.Validation.payments.pojo.PaymentRequest;
import com.Validation.payments.repository.interfaces.RiskScoreRepository;
import com.Validation.payments.repository.interfaces.UserTransactionStatsRepository;
import com.Validation.payments.service.BusinessValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudCheckValidator implements BusinessValidator {

    private final List<FraudRule> fraudRules;
    private final UserTransactionStatsRepository statsRepository;
    private final RiskScoreRepository riskScoreRepository;
    private final FraudReasoningService fraudReasoningService;

    @Value("${fraud.threshold.medium:30}")
    private int mediumThreshold;

    @Value("${fraud.threshold.high:70}")
    private int highThreshold;

    @Override
    public void validate(PaymentRequest paymentRequest) {

        String endUserID = paymentRequest.getUser().getEndUserID();
        long amount = paymentRequest.getPayment().getAmount();

        UserTransactionStatsEntity stats = statsRepository.findByEndUserID(endUserID).orElse(null);
        long txnCountLastHour = statsRepository.countTransactionsInLastHour(endUserID);

        List<FraudRuleResult> results = fraudRules.stream()
                .map(rule -> rule.evaluate(paymentRequest, stats, txnCountLastHour))
                .toList();

        int totalScore = results.stream()
                .filter(FraudRuleResult::isTriggered)
                .mapToInt(FraudRuleResult::getPoints)
                .sum();
        totalScore = Math.min(100, totalScore);

        String verdict = totalScore >= highThreshold ? "HIGH"
                : totalScore >= mediumThreshold ? "MEDIUM"
                : "LOW";

        List<FraudRuleResult> triggered = results.stream()
                .filter(FraudRuleResult::isTriggered)
                .toList();

        String reasonSummary;
        if ("MEDIUM".equals(verdict) && !triggered.isEmpty()) {
            log.info("Calling FraudResoiningService");
            reasonSummary = fraudReasoningService.explainRisk(endUserID, amount, triggered);
        } else if ("HIGH".equals(verdict)) {
            reasonSummary = "Multiple high-confidence fraud signals triggered - blocked automatically.";
        } else {
            reasonSummary = "No significant fraud signals detected.";
        }

        log.info("Fraud check for endUserID: {} | score: {} | verdict: {}", endUserID, totalScore, verdict);

        persistRiskScore(paymentRequest, totalScore, verdict, reasonSummary, triggered);
        updateUserStats(endUserID, amount, paymentRequest.getPayment().getPaymentMethod(), stats);

        if ("HIGH".equals(verdict)) {
            throw new PaymentValidationException(
                    ErrorCode.HIGH_RISK_TRANSACTION_BLOCKED.getCode(),
                    ErrorCode.HIGH_RISK_TRANSACTION_BLOCKED.getMessage(),
                    HttpStatus.FORBIDDEN
            );
        }
    }

    private void persistRiskScore(PaymentRequest paymentRequest, int score, String verdict,
                                   String reasonSummary, List<FraudRuleResult> triggered) {

        RiskScoreEntity entity = new RiskScoreEntity();
        entity.setMerchantTxnReference(paymentRequest.getPayment().getMerchantTxnRef());
        entity.setEndUserID(paymentRequest.getUser().getEndUserID());
        entity.setScore(score);
        entity.setVerdict(verdict);
        entity.setReasonSummary(reasonSummary);
        entity.setCreatedDate(LocalDateTime.now());

        int riskScoreId = riskScoreRepository.saveRiskScore(entity);

        for (FraudRuleResult result : triggered) {
            riskScoreRepository.saveFlaggedReason(riskScoreId, result.getRuleName(), result.getDetails());
        }
    }

    private void updateUserStats(String endUserID, long amount, String paymentMethod,
                                  UserTransactionStatsEntity existing) {

        UserTransactionStatsEntity stats = existing != null ? existing : new UserTransactionStatsEntity();
        stats.setEndUserID(endUserID);

        long newCount = stats.getTransactionCount() + 1;
        double oldAvg = stats.getAvgAmount();
        double newAvg = oldAvg + (amount - oldAvg) / newCount;

        // simple running variance update (not exact Welford but good enough for this rule set)
        double oldVariance = stats.getStdDevAmount() * stats.getStdDevAmount();
        double newVariance = ((newCount - 1) * oldVariance + (amount - oldAvg) * (amount - newAvg)) / newCount;

        stats.setTransactionCount(newCount);
        stats.setAvgAmount(newAvg);
        stats.setStdDevAmount(Math.sqrt(Math.max(0, newVariance)));
        stats.setLastTransactionAt(LocalDateTime.now());

        String known = stats.getKnownMerchantCategories();
        if (known == null || known.isBlank()) {
            stats.setKnownMerchantCategories(paymentMethod);
        } else if (!known.contains(paymentMethod)) {
            stats.setKnownMerchantCategories(known + "," + paymentMethod);
        }

        statsRepository.upsert(stats);
    }
}
