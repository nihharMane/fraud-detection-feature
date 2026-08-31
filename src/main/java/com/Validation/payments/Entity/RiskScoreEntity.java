package com.Validation.payments.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "risk_score")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RiskScoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "merchantTxnReference", nullable = false)
    private String merchantTxnReference;

    @Column(name = "endUserID", nullable = false)
    private String endUserID;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "verdict", nullable = false)
    private String verdict; // LOW / MEDIUM / HIGH

    @Column(name = "reasonSummary", columnDefinition = "TEXT")
    private String reasonSummary;

    @Column(name = "createdDate", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "overridden", nullable = false)
    private boolean overridden;

    @Column(name = "overrideNote")
    private String overrideNote;
}