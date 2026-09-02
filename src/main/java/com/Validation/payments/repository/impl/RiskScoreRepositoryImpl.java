package com.Validation.payments.repository.impl;

import com.Validation.payments.Constants.ErrorCode;
import com.Validation.payments.Entity.RiskScoreEntity;
import com.Validation.payments.Exception.PaymentValidationException;
import com.Validation.payments.repository.interfaces.RiskScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RiskScoreRepositoryImpl implements RiskScoreRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public int saveRiskScore(RiskScoreEntity entity) {

        log.info("Saving RiskScore for txnRef: {}", entity.getMerchantTxnReference());

        String sql = """
    INSERT INTO risk_score
    (merchantTxnReference, endUserID, score, verdict, reasonSummary, createdDate, overridden)
    VALUES
    (:txnRef, :endUserID, :score, :verdict, :reasonSummary, :createdDate, :overridden)
    """;

        Map<String, Object> params = new HashMap<>();
        params.put("txnRef", entity.getMerchantTxnReference());
        params.put("endUserID", entity.getEndUserID());
        params.put("score", entity.getScore());
        params.put("verdict", entity.getVerdict());
        params.put("reasonSummary", entity.getReasonSummary());
        params.put("createdDate", entity.getCreatedDate());
        params.put("overridden", false);

        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(sql, new MapSqlParameterSource(params), keyHolder);

            Number generatedId = keyHolder.getKey();
            if (generatedId != null) {
                log.info("RiskScore saved. Generated ID: {}", generatedId.intValue());
                return generatedId.intValue();
            }

            throw new PaymentValidationException(
                    ErrorCode.FAILED_TO_SAVE.getCode(),
                    ErrorCode.FAILED_TO_SAVE.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

        } catch (Exception ex) {
            log.error("Error inserting risk score", ex);
            throw new RuntimeException("Database error while inserting risk score", ex);
        }
    }

    @Override
    public void saveFlaggedReason(int riskScoreId, String ruleTriggered, String details) {

        log.info("Saving FlaggedReason for riskScoreId: {}, rule: {}", riskScoreId, ruleTriggered);

        String sql = """
        INSERT INTO flagged_reason
        (riskScoreId, ruleTriggered, details)
        VALUES
        (:riskScoreId, :ruleTriggered, :details)
        """;

        Map<String, Object> params = new HashMap<>();
        params.put("riskScoreId", riskScoreId);
        params.put("ruleTriggered", ruleTriggered);
        params.put("details", details);

        try {
            jdbcTemplate.update(sql, params);
        } catch (Exception ex) {
            log.error("Error inserting flagged reason", ex);
            throw new RuntimeException("Database error while inserting flagged reason", ex);
        }
    }

    @Override
    public java.util.List<RiskScoreEntity> findFlagged(int limit, int offset) {

        String sql = """
        SELECT id, merchantTxnReference, endUserID, score, verdict, reasonSummary,
               createdDate, overridden, overrideNote
        FROM risk_score
        WHERE verdict IN ('MEDIUM', 'HIGH')
        ORDER BY createdDate DESC
        LIMIT :limit OFFSET :offset
        """;

        Map<String, Object> params = Map.of("limit", limit, "offset", offset);

        return jdbcTemplate.query(sql, params, this::mapRow);
    }

    @Override
    public java.util.Optional<RiskScoreEntity> findByMerchantTxnReference(String merchantTxnReference) {

        String sql = """
        SELECT id, merchantTxnReference, endUserID, score, verdict, reasonSummary,
               createdDate, overridden, overrideNote
        FROM risk_score
        WHERE merchantTxnReference = :txnRef
        ORDER BY createdDate DESC
        LIMIT 1
        """;

        Map<String, Object> params = Map.of("txnRef", merchantTxnReference);

        return jdbcTemplate.query(sql, params, this::mapRow).stream().findFirst();
    }

    @Override
    public java.util.List<com.Validation.payments.Entity.FlaggedReasonEntity> findFlaggedReasonsByRiskScoreId(int riskScoreId) {

        String sql = """
        SELECT id, riskScoreId, ruleTriggered, details
        FROM flagged_reason
        WHERE riskScoreId = :riskScoreId
        """;

        Map<String, Object> params = Map.of("riskScoreId", riskScoreId);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            com.Validation.payments.Entity.FlaggedReasonEntity entity = new com.Validation.payments.Entity.FlaggedReasonEntity();
            entity.setId(rs.getInt("id"));
            entity.setRiskScoreId(rs.getInt("riskScoreId"));
            entity.setRuleTriggered(rs.getString("ruleTriggered"));
            entity.setDetails(rs.getString("details"));
            return entity;
        });
    }

    @Override
    public void markOverridden(int riskScoreId, String overrideNote) {

        String sql = """
        UPDATE risk_score
        SET overridden = true, overrideNote = :overrideNote
        WHERE id = :id
        """;

        Map<String, Object> params = Map.of("id", riskScoreId, "overrideNote", overrideNote);

        int updated = jdbcTemplate.update(sql, params);
        if (updated == 0) {
            throw new PaymentValidationException(
                    ErrorCode.RECORD_NOT_FOUND.getCode(),
                    ErrorCode.RECORD_NOT_FOUND.getMessage(),
                    HttpStatus.NOT_FOUND
            );
        }
    }

    private RiskScoreEntity mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        RiskScoreEntity entity = new RiskScoreEntity();
        entity.setId(rs.getInt("id"));
        entity.setMerchantTxnReference(rs.getString("merchantTxnReference"));
        entity.setEndUserID(rs.getString("endUserID"));
        entity.setScore(rs.getInt("score"));
        entity.setVerdict(rs.getString("verdict"));
        entity.setReasonSummary(rs.getString("reasonSummary"));
        entity.setCreatedDate(rs.getTimestamp("createdDate") != null
                ? rs.getTimestamp("createdDate").toLocalDateTime() : null);
        entity.setOverridden(rs.getBoolean("overridden"));
        entity.setOverrideNote(rs.getString("overrideNote"));
        return entity;
    }
}