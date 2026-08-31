package com.Validation.payments.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_transaction_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserTransactionStatsEntity {

    @Id
    @Column(name = "endUserID")
    private String endUserID;

    @Column(name = "avgAmount")
    private double avgAmount;

    @Column(name = "stdDevAmount")
    private double stdDevAmount;

    @Column(name = "transactionCount")
    private long transactionCount;

    @Column(name = "lastTransactionAt")
    private LocalDateTime lastTransactionAt;

    @Column(name = "knownMerchantCategories", columnDefinition = "TEXT")
    private String knownMerchantCategories; // comma-separated, simple for now
}
