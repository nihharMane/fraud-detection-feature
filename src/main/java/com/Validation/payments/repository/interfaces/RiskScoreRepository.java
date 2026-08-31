package com.Validation.payments.repository.interfaces;

import com.Validation.payments.Entity.FlaggedReasonEntity;
import com.Validation.payments.Entity.RiskScoreEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RiskScoreRepository {

    int saveRiskScore(RiskScoreEntity entity);

    void saveFlaggedReason(int riskScoreId, String ruleTriggered, String details);

    List<RiskScoreEntity> findFlagged(int limit, int offset);

    Optional<RiskScoreEntity> findByMerchantTxnReference(String merchantTxnReference);

    List<FlaggedReasonEntity> findFlaggedReasonsByRiskScoreId(int riskScoreId);

    void markOverridden(int riskScoreId, String overrideNote);
}