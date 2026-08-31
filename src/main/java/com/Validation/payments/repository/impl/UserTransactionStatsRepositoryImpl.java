package com.Validation.payments.repository.impl;

import com.Validation.payments.Entity.UserTransactionStatsEntity;
import com.Validation.payments.repository.interfaces.UserTransactionStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserTransactionStatsRepositoryImpl implements UserTransactionStatsRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Optional<UserTransactionStatsEntity> findByEndUserID(String endUserID) {

        String sql = """
        SELECT endUserID, avgAmount, stdDevAmount, transactionCount, lastTransactionAt, knownMerchantCategories
        FROM user_transaction_stats
        WHERE endUserID = :endUserID
        """;

        Map<String, Object> params = Map.of("endUserID", endUserID);

        List<UserTransactionStatsEntity> results = jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            UserTransactionStatsEntity entity = new UserTransactionStatsEntity();
            entity.setEndUserID(rs.getString("endUserID"));
            entity.setAvgAmount(rs.getDouble("avgAmount"));
            entity.setStdDevAmount(rs.getDouble("stdDevAmount"));
            entity.setTransactionCount(rs.getLong("transactionCount"));
            entity.setLastTransactionAt(rs.getTimestamp("lastTransactionAt") != null
                    ? rs.getTimestamp("lastTransactionAt").toLocalDateTime() : null);
            entity.setKnownMerchantCategories(rs.getString("knownMerchantCategories"));
            return entity;
        });

        return results.stream().findFirst();
    }

    @Override
    public void upsert(UserTransactionStatsEntity entity) {

        log.info("Upserting UserTransactionStats for endUserID: {}", entity.getEndUserID());

        String sql = """
        INSERT INTO user_transaction_stats
        (endUserID, avgAmount, stdDevAmount, transactionCount, lastTransactionAt, knownMerchantCategories)
        VALUES
        (:endUserID, :avgAmount, :stdDevAmount, :transactionCount, :lastTransactionAt, :knownMerchantCategories)
        ON DUPLICATE KEY UPDATE
            avgAmount = :avgAmount,
            stdDevAmount = :stdDevAmount,
            transactionCount = :transactionCount,
            lastTransactionAt = :lastTransactionAt,
            knownMerchantCategories = :knownMerchantCategories
        """;

        Map<String, Object> params = new HashMap<>();
        params.put("endUserID", entity.getEndUserID());
        params.put("avgAmount", entity.getAvgAmount());
        params.put("stdDevAmount", entity.getStdDevAmount());
        params.put("transactionCount", entity.getTransactionCount());
        params.put("lastTransactionAt", entity.getLastTransactionAt());
        params.put("knownMerchantCategories", entity.getKnownMerchantCategories());

        jdbcTemplate.update(sql, new MapSqlParameterSource(params));
    }

    @Override
    public long countTransactionsInLastHour(String endUserID) {

        String sql = """
        SELECT COUNT(*) FROM merchant_payment_request
        WHERE endUserID = :endUserID
        AND creationDate >= :since
        """;

        Map<String, Object> params = Map.of(
                "endUserID", endUserID,
                "since", LocalDateTime.now().minusHours(1)
        );

        Long count = jdbcTemplate.queryForObject(sql, params, Long.class);
        return count != null ? count : 0L;
    }
}
