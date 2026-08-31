package com.Validation.payments.repository.interfaces;

import com.Validation.payments.Entity.UserTransactionStatsEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserTransactionStatsRepository {

    Optional<UserTransactionStatsEntity> findByEndUserID(String endUserID);

    void upsert(UserTransactionStatsEntity entity);

    long countTransactionsInLastHour(String endUserID);
}
